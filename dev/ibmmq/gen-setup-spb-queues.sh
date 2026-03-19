#!/usr/bin/env bash
# Generates dev/ibmmq/20-spb-queues.mqsc (queues and permissions).
#
# Input: .env in the repo root
#  - IBMMQ_HOST, IBMMQ_PORT, IBMMQ_CHANNEL, IBMMQ_USER, IBMMQ_QMGR_NAME (used for validation/comments)
#  - For each queue category:
#      IBMMQ_QL_<CATEGORY>_NAME  (e.g. QL.REQ.00000000.99999999.01)
#      IBMMQ_QL_<CATEGORY>_TYPE  (human label, e.g. request/response/report/support)
#    Where CATEGORY is one of: REQ, RSP, REP, SUP
#
# Optional multi-entry support (up to 50, contiguous):
#  - IBMMQ_QL_<CATEGORY>_1_NAME / _1_TYPE, IBMMQ_QL_<CATEGORY>_2_NAME / _2_TYPE, ...
#
# Output: dev/ibmmq/20-spb-queues.mqsc

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

ENV_FILE="$REPO_ROOT/.env"
EXAMPLE_FILE="$REPO_ROOT/.env.example"
OUT_FILE="$SCRIPT_DIR/20-spb-queues.mqsc"

if [[ ! -f "$ENV_FILE" ]]; then
  if [[ -f "$EXAMPLE_FILE" ]]; then
    echo ".env not found. Bootstrapping from .env.example..."
    cp "$EXAMPLE_FILE" "$ENV_FILE"
  else
    echo ".env file not found and .env.example is also missing." >&2
    exit 1
  fi
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

missing=()
[[ -z "${IBMMQ_HOST:-}" ]] && missing+=("IBMMQ_HOST")
[[ -z "${IBMMQ_PORT:-}" ]] && missing+=("IBMMQ_PORT")
[[ -z "${IBMMQ_CHANNEL:-}" ]] && missing+=("IBMMQ_CHANNEL")
[[ -z "${IBMMQ_USER:-}" ]] && missing+=("IBMMQ_USER")
[[ -z "${IBMMQ_QMGR_NAME:-}" ]] && missing+=("IBMMQ_QMGR_NAME")

if (( ${#missing[@]} > 0 )); then
  echo ".env is missing required variables: ${missing[*]}" >&2
  exit 1
fi

split_tokens() {
  # Splits a string by any amount of whitespace and prints each token on a line.
  local s="$1"
  s="${s#"${s%%[![:space:]]*}"}"
  s="${s%"${s##*[![:space:]]}"}"
  [[ -z "$s" ]] && return 0
  # shellcheck disable=SC2206
  printf "%s\n" $s
}

declare -a INBOUND_QUEUES=()
declare -a INBOUND_LABELS=()

add_category_queues() {
  local cat="$1" # REQ|RSP|REP|SUP

  local base_name_var="IBMMQ_QL_${cat}_NAME"
  local base_type_var="IBMMQ_QL_${cat}_TYPE"
  local label="${!base_type_var:-$cat}"
  local base_names="${!base_name_var:-}"

  for q in $(split_tokens "$base_names"); do
    [[ -z "$q" ]] && continue
    INBOUND_QUEUES+=("$q")
    INBOUND_LABELS+=("$label")
  done

  # Optional numbered entries: _1_NAME/_1_TYPE ... up to 50 (contiguous).
  local i
  for i in {1..50}; do
    local name_var="IBMMQ_QL_${cat}_${i}_NAME"
    local type_var="IBMMQ_QL_${cat}_${i}_TYPE"
    local names_i="${!name_var:-}"
    [[ -z "$names_i" ]] && break

    local label_i="${!type_var:-$label}"
    for q in $(split_tokens "$names_i"); do
      [[ -z "$q" ]] && continue
      INBOUND_QUEUES+=("$q")
      INBOUND_LABELS+=("$label_i")
    done
  done
}

add_category_queues "REQ"
add_category_queues "RSP"
add_category_queues "REP"
add_category_queues "SUP"

if [[ ${#INBOUND_QUEUES[@]} -eq 0 ]]; then
  # Backward-compatible fallback (legacy format):
  # - IBMMQ_QUEUES="QL.REQ....,QL.RSP...." (comma-separated)
  # - IBMMQ_QL_REQ/RSP/REP/SUP="QL.REQ... QL.REQ..." (whitespace-separated)
  if [[ -n "${IBMMQ_QUEUES:-}" ]]; then
    echo "No IBMMQ_QL_*_NAME/TYPE queues found in .env. Falling back to legacy IBMMQ_QUEUES..." >&2
    IFS=',' read -ra IBMMQ_QUEUE_LIST <<< "$IBMMQ_QUEUES"
    for q in "${IBMMQ_QUEUE_LIST[@]}"; do
      q_trimmed="${q#"${q%%[![:space:]]*}"}"
      q_trimmed="${q_trimmed%"${q_trimmed##*[![:space:]]}"}"
      [[ -z "$q_trimmed" ]] && continue
      INBOUND_QUEUES+=("$q_trimmed")
      INBOUND_LABELS+=("legacy")
    done
  elif [[ -n "${IBMMQ_QL_REQ:-}${IBMMQ_QL_RSP:-}${IBMMQ_QL_REP:-}${IBMMQ_QL_SUP:-}" ]]; then
    echo "No IBMMQ_QL_*_NAME/TYPE queues found in .env. Falling back to legacy IBMMQ_QL_* lists..." >&2
    for q in $(split_tokens "${IBMMQ_QL_REQ:-}"); do INBOUND_QUEUES+=("$q"); INBOUND_LABELS+=("legacy"); done
    for q in $(split_tokens "${IBMMQ_QL_RSP:-}"); do INBOUND_QUEUES+=("$q"); INBOUND_LABELS+=("legacy"); done
    for q in $(split_tokens "${IBMMQ_QL_REP:-}"); do INBOUND_QUEUES+=("$q"); INBOUND_LABELS+=("legacy"); done
    for q in $(split_tokens "${IBMMQ_QL_SUP:-}"); do INBOUND_QUEUES+=("$q"); INBOUND_LABELS+=("legacy"); done
  else
    echo "No inbound queues configured. Set IBMMQ_QL_<REQ|RSP|REP|SUP>_NAME and *_TYPE in .env (or update/delete legacy IBMMQ_QUEUES)." >&2
    exit 1
  fi
fi

# Deduplicate inbound queues while preserving the first label encountered.
declare -a SEEN_INBOUND=()
declare -a QUEUES=()
declare -a QUEUE_LABELS=()

for idx in "${!INBOUND_QUEUES[@]}"; do
  q="${INBOUND_QUEUES[$idx]}"
  [[ -z "$q" ]] && continue

  found=false
  for s in "${SEEN_INBOUND[@]}"; do
    if [[ "$s" == "$q" ]]; then
      found=true
      break
    fi
  done

  if [[ "$found" != "true" ]]; then
    SEEN_INBOUND+=("$q")
    QUEUES+=("$q")
    QUEUE_LABELS+=("${INBOUND_LABELS[$idx]}")
  fi
done

# Always include the stage queue (consumed by the StageServer).
STAGE_QUEUE="QL.STG.NOTIFY.01"
if [[ "${SEEN_INBOUND[*]}" != *"$STAGE_QUEUE"* ]]; then
  QUEUES+=("$STAGE_QUEUE")
  QUEUE_LABELS+=("stage")
fi

# Optional generic queues used by SPB flows (if your environment needs them).
EXTRA_QUEUES=("QL.SUP.LOCAL.01" "QL.REP.LOCAL.01")
for eq in "${EXTRA_QUEUES[@]}"; do
  found=false
  for s in "${SEEN_INBOUND[@]}"; do
    if [[ "$s" == "$eq" ]]; then
      found=true
      break
    fi
  done
  if [[ "$found" != "true" ]]; then
    SEEN_INBOUND+=("$eq")
    QUEUES+=("$eq")
    QUEUE_LABELS+=("extra")
  fi
done

# Outbound queues derived from inbound QL.* names.
declare -a SEEN_OUTBOUND=()
declare -a OUTBOUND_QUEUES=()

for q in "${QUEUES[@]}"; do
  if [[ "$q" =~ ^QL\.(REQ|RSP|REP|SUP)\.([0-9]{8})\.([0-9]{8})\.([0-9]{2})$ ]]; then
    qr="QR.${BASH_REMATCH[1]}.${BASH_REMATCH[3]}.${BASH_REMATCH[2]}.${BASH_REMATCH[4]}"
    found=false
    for s in "${SEEN_OUTBOUND[@]}"; do
      if [[ "$s" == "$qr" ]]; then
        found=true
        break
      fi
    done
    if [[ "$found" != "true" ]]; then
      SEEN_OUTBOUND+=("$qr")
      OUTBOUND_QUEUES+=("$qr")
    fi
  fi
done

ALL_QUEUES=("${QUEUES[@]}" "${OUTBOUND_QUEUES[@]}")

{
  echo '* Queues and authorizations for the SPB app on localhost (dev)'
  echo '* Generated by dev/ibmmq/gen-setup-spb-queues.sh from .env (IBMMQ_QL_*_NAME/TYPE).'
  echo "* Channel ${IBMMQ_CHANNEL}; Queue manager: ${IBMMQ_QMGR_NAME}; User: ${IBMMQ_USER}"
  echo '*'
  echo '* Convention: QL.{TYPE}.{SENDER}.{RECIPIENT}.NN = inbound; QR.* = outbound (application send).'
  echo '*'

  for idx in "${!QUEUES[@]}"; do
    q="${QUEUES[$idx]}"
    label="${QUEUE_LABELS[$idx]:-$q}"
    echo "* Inbound (${label}): ${q}"
    echo "DEFINE QLOCAL($q) REPLACE"
  done

  if [[ ${#OUTBOUND_QUEUES[@]} -gt 0 ]]; then
    echo '*'
    for q in "${OUTBOUND_QUEUES[@]}"; do
      echo "* Outbound (derived): ${q}"
      echo "DEFINE QLOCAL($q) REPLACE"
    done
  fi

  echo '*'
  echo '* Permissions for IBMMQ_USER on all queues'
  echo "SET AUTHREC OBJTYPE(QMGR) PRINCIPAL('${IBMMQ_USER}') AUTHADD(ALL)"
  for q in "${ALL_QUEUES[@]}"; do
    echo "SET AUTHREC PROFILE($q) OBJTYPE(QUEUE) PRINCIPAL('${IBMMQ_USER}') AUTHADD(ALL)"
  done

  echo 'REFRESH SECURITY (*)'
} > "$OUT_FILE"

echo "→ $OUT_FILE generated with ${#QUEUES[@]} inbound queues + ${#OUTBOUND_QUEUES[@]} outbound (${#ALL_QUEUES[@]} total)."

