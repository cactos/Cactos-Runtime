#! /bin/bash 


print_usage() {
	echo "usage <command> (set|get) filename configparam"
}

check_parameters(){
	echo "checking parameters: filename"
	if [ -z ${1+x} ]; then 
		echo "filename is not set -> leaving"
		print_usage
		exit 12
	fi
	if [ ! -f $1 ]; then
		echo "File $FILENAME not found or not a file -> leaving"
		print_usage
		exit 14
	fi

	echo "checking parameters: configoption"
	if [ -z ${2+x} ]; then 
		echo "config option is not set -> leaving"
		print_usage
		exit 16
	fi

	FILENAME=$1
	CONFIG_OPTION=$2
}

do_set() {
	echo "checking value: $1"
	if [ -z ${1+x} ]; then 
		echo "value is not set -> leaving"
		exit 18
	fi
	echo "setting '$CONFIG_OPTION' in $FILENAME to $1"
	FILENAME_BKP="${FILENAME}.bkp"
	echo "copying "
	cp $FILENAME ${FILENAME_BKP}
	sed -i -e "/^\s*${CONFIG_OPTION}\s*/d" ${FILENAME_BKP}
	echo "${CONFIG_OPTION} = $1 # set by script" >> ${FILENAME_BKP}
	cp ${FILENAME_BKP} ${FILENAME}
}

do_get() {
	echo "getting '$CONFIG_OPTION' from $FILENAME"
	RESULT=$(grep -e "^[[:blank:]]*$CONFIG_OPTION" $FILENAME | sed -e 's/^\s*//g' -e 's/\#.*//g'| tail -n 1)
	# assuming: one line with one '=' in the middle
	echo "filtering intermediate result: $RESULT"
	RESULT=$(echo "$RESULT" | sed -e 's/^.*=//g' -e 's/\s*$//g')
	echo -n $RESULT 1>&2
}

case "$1" in
  get)
	  check_parameters $2 $3
	  do_get 
	  echo "done"
	  ;;
  set)
	  check_parameters $2 $3
	  do_set $4
	  echo "done"
	  ;;
  *)
	  print_usage
	  exit 3
	  ;;
esac

exit 0

