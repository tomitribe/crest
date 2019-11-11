#!/bin/bash


function _propose_files() {
  local cur=${COMP_WORDS[COMP_CWORD]}
  COMPREPLY=($(compgen -f "$cur"))
}


function _propose_flags() {
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


function _propose_flag_values() {
  local VALUES="$@"
  local cur=${COMP_WORDS[COMP_CWORD]}

  cur="$(echo "$cur" | perl -pe 's/[^=]+=//')"
  COMPREPLY=($(compgen -W "${VALUES[*]}" "$cur"))
}


function _propose_flag_file_values() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  cur="$(echo "$cur" | perl -pe 's/[^=]+=//')"
  COMPREPLY=($(compgen -f "$cur"))
}

function _defaults() {
  local cur=${COMP_WORDS[COMP_CWORD]}
  local args_length=${#COMP_WORDS[@]}

  local COMMANDS=(
    help
    primitives
    objects
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
      _defaults_$CMD
      return
    }
  done

  COMPREPLY=()
}


function _defaults_help() {
  _propose_files
}

function _defaults_primitives() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
  --oBoolean=*) _propose_flag_values "true" "false" ;;
  --oByte=*) _propose_flag_values "<byte>" ;;
  --oCharacter=*) _propose_flag_values "<char>" ;;
  --oDouble=*) _propose_flag_values "<double>" ;;
  --oFloat=*) _propose_flag_values "<float>" ;;
  --oInteger=*) _propose_flag_values "<int>" ;;
  --oLong=*) _propose_flag_values "<long>" ;;
  --oShort=*) _propose_flag_values "<short>" ;;
  -*) _propose_flags "--oBoolean=" "--oByte=" "--oCharacter=" "--oDouble=" "--oFloat=" "--oInteger=" "--oLong=" "--oShort=";;
  *) _propose_files ;;
  esac

}

function _defaults_objects() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
  --oByte=*) _propose_flag_values "<Byte>" ;;
  --oCharacter=*) _propose_flag_values "<Character>" ;;
  --oDouble=*) _propose_flag_values "<Double>" ;;
  --oFloat=*) _propose_flag_values "<Float>" ;;
  --oInteger=*) _propose_flag_values "<Integer>" ;;
  --oLong=*) _propose_flag_values "<Long>" ;;
  --oShort=*) _propose_flag_values "<Short>" ;;
  --oURI=*) _propose_flag_values "<URI>" ;;
  --oURL=*) _propose_flag_values "<URL>" ;;
  -*) _propose_flags "--oByte=" "--oCharacter=" "--oDouble=" "--oFloat=" "--oInteger=" "--oLong=" "--oShort=" "--oURI=" "--oURL=";;
  *) _propose_files ;;
  esac

}

complete -F _defaults defaults
