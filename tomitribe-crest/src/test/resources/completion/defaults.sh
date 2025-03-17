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
  --oByte=*) _propose_flag_values "<byte>" ;;
  --oBoolean=*) _propose_flag_values "true" "false" ;;
  --oCharacter=*) _propose_flag_values "<char>" ;;
  --oShort=*) _propose_flag_values "<short>" ;;
  --oInteger=*) _propose_flag_values "<int>" ;;
  --oLong=*) _propose_flag_values "<long>" ;;
  --oFloat=*) _propose_flag_values "<float>" ;;
  --oDouble=*) _propose_flag_values "<double>" ;;
  -*) _propose_flags "--oByte=" "--oBoolean=" "--oCharacter=" "--oShort=" "--oInteger=" "--oLong=" "--oFloat=" "--oDouble=";;
  *) _propose_files ;;
  esac

}

function _defaults_objects() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
  --oURI=*) _propose_flag_values "<URI>" ;;
  --oURL=*) _propose_flag_values "<URL>" ;;
  --oByte=*) _propose_flag_values "<Byte>" ;;
  --oCharacter=*) _propose_flag_values "<Character>" ;;
  --oShort=*) _propose_flag_values "<Short>" ;;
  --oInteger=*) _propose_flag_values "<Integer>" ;;
  --oLong=*) _propose_flag_values "<Long>" ;;
  --oFloat=*) _propose_flag_values "<Float>" ;;
  --oDouble=*) _propose_flag_values "<Double>" ;;
  -*) _propose_flags "--oURI=" "--oURL=" "--oByte=" "--oCharacter=" "--oShort=" "--oInteger=" "--oLong=" "--oFloat=" "--oDouble=";;
  *) _propose_files ;;
  esac

}

complete -F _defaults defaults
