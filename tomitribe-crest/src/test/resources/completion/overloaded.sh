#!/bin/bash


function _overloaded__propose_files() {
  local cur=${COMP_WORDS[COMP_CWORD]}
  COMPREPLY=($(compgen -f "$cur"))
}


function _overloaded__propose_flags() {
  local FLAGS="$@"
  local cur=${COMP_WORDS[COMP_CWORD]}

  # minus flags we've used
  for ((i = 0; i < ${#COMP_WORDS[*]} - 1; i++)); do
    n="${COMP_WORDS[$i]}"
    [[ "$n" == -* ]] && {
      n="${n/=*/=}"
      FLAGS=("${FLAGS[@]/$n/}")
    }
  done

  COMPREPLY=($(compgen -W "${FLAGS[*]}" -- "$cur"))
}


function _overloaded__propose_flag_values() {
  local VALUES="$@"
  local cur=${COMP_WORDS[COMP_CWORD]}

  cur="$(echo "$cur" | perl -pe 's/[^=]+=//')"
  COMPREPLY=($(compgen -W "${VALUES[*]}" "$cur"))
}


function _overloaded__propose_flag_file_values() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  cur="$(echo "$cur" | perl -pe 's/[^=]+=//')"
  COMPREPLY=($(compgen -f "$cur"))
}

function _overloaded() {

  local cur=${COMP_WORDS[COMP_CWORD]}

  # Find the index of the last global flag
  local LAST_GLOBAL_FLAG_INDEX=0

  for ((i = 1; i < ${#COMP_WORDS[@]}; i++)); do
    [[ "${COMP_WORDS[i]}" != -* ]] && break
    ((LAST_GLOBAL_FLAG_INDEX++))
  done

  # If the current completion is a flag and that is before any subsequent
  # commands, we do global flag completion.
  if [[ "$cur" == -* ]] && (( COMP_CWORD <= LAST_GLOBAL_FLAG_INDEX )); then

    # Remove any command arguments so their flags do not influence
    # logic in _propose_flags that tries not to repeat flags
    COMP_WORDS=("${COMP_WORDS[@]:0:LAST_GLOBAL_FLAG_INDEX+1}")

    _overloaded__global_flags
    return
  fi

  # If there are global flags, trim them out adjust the COMP_CWORD index
  if (( LAST_GLOBAL_FLAG_INDEX > 0 )); then
    COMP_WORDS=("${COMP_WORDS[0]}" "${COMP_WORDS[@]:LAST_GLOBAL_FLAG_INDEX+1}")
    COMP_CWORD=$(( COMP_CWORD - LAST_GLOBAL_FLAG_INDEX  ))
  fi

  local args_length=${#COMP_WORDS[@]}
  local COMMANDS=(
    help
    push
  )

  # List the commands
  [ $args_length -lt 3 ] && {
    COMPREPLY=($(compgen -W "${COMMANDS[*]}" "$cur"))
    return
  }

  # Command chosen.  Delegate to its completion function

  # Verify the command is one we know and execute the
  # function that performs its completion
  local CMD=${COMP_WORDS[1]}
  for n in "${COMMANDS[@]}"; do
    [ "$CMD" = "$n" ] && {
      CMD="$(echo "$CMD" | perl -pe 's,[^a-zA-Z0-9],,g')"
      _overloaded_$CMD
      return
    }
  done

  COMPREPLY=()
}


function _overloaded__global_flags() {
  COMPREPLY=()
}

function _overloaded_help() {
  _overloaded__propose_files
}

function _overloaded_push() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
  --verbose=*) _overloaded__propose_flag_file_values ;;
  -u=*) _overloaded__propose_flag_file_values ;;
  -*) _overloaded__propose_flags "--verbose=" "-u=";;
  *) _overloaded__propose_files ;;
  esac

}

complete -F _overloaded overloaded
