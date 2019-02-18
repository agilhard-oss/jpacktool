#!/bin/sh

app=sample
os=`uname`
if [ "${os}" == "Darwin" ]; then
os="mac"
elif [ "${os} == "Linux ]; then
os="linux"
fi

usage() {
    echo
    echo "usage:"
    echo "update.sh -server <server> -port <port> [-install] [-wait] [-h]"
    exit 0;
}

wait="false"
install="false"

until [ -z "$1" ]; do
  case $1 in
      "-h")
	  shift
	  usage
      ;;
      "-server")
	  shift
	  if [ $# -eq 0 ]; then
	      usage
	  fi
	  server=$1
	  shift
      ;;
      "-port")
	  shift
	  if [ $# -eq 0 ]; then
	      usage
	  fi
	  port=$1
	  shift
      ;;
      "-install")
	  shift
	  install="true"
      ;;
      "-wait")
	  shift
	  wait="true"
      ;;
  esac
done    
if [ "$server" == "" ]; then
    echo -n "server: "
    read server
fi
if [ "$port" == "" ]; then
    echo -n "port: "
    read port
fi


Dest="${HOME}/${app}-runtime"
ScriptDir=$(readlink -f "$0")
ScriptDir=$(dirname "${ScriptDir}") 

Update() {
BaseUrl="http://${server}:${port}/${app}/bootstrap/${os}"

echo "BaseUrl=${BaseUrl}"

cd "${ScriptDir}"

output="${ScriptDir}/update.sha256"

url="${BaseUrl}/update.sha256"
curl -s $url -o "${output}"
if [ $? -ne 0 ]; then
    echo "error downloading ${url}"
    exit -1
fi
sha=`cat update.sha256`
mustUpdate="true" 

Check="${Dest}/update.sha256"

if [ -f "${Check}" ]; then
    sha2=`cat "${Check}"`
    if [ "${sha}" == "${sha2}" ]; then
	mustUpdate="false"
    else
	echo "hash values differ";
	echo "expected: $sha";
	echo "     got: $sha2";
    fi
else 
    echo "checksum file not present ${Check}"
fi

if [ ${mustUpdate} == "true" ]; then
    echo "Runtime Update required"
    url="${BaseUrl}/update.zip"
    output2="${ScriptDir}/update.zip"
    echo "downloading ${url}"
    curl -s $url -o "${output2}"
     if [ $? -ne 0 ]; then
	 echo "error downloading ${url}"
	 exit -1
     fi
     sha2=`sha256sum "${output2}" | awk '{print $1}'`
    if [ "${sha}" != "${sha2}" ]; then
	echo "hash values differ";
	echo "expected: $sha";
	echo "     got: $sha2";
	exit -1
    else
	echo "SHA Hash Values matches: $sha"; 
    fi

    rm -rf "${Dest}"
    unzip -q "${output2}" -d "${Dest}"
    if [ $? -ne 0 ]; then
	echo "error unzipping ${output2}"
	exit -1
    fi
    cp "${output}" "${Check}"

else
    echo "No Runtime Update required"
fi


}

Launch() {
    cd $Dest
    bin/java --module-path app/jar_auto -splash:conf/splash.gif net.agilhard.jpacktool.util.update4j.JPacktoolBootstrap  $*
}

Install() {
    if [ "${os}" == "mac" ]; then
         echo "Install not implemented for Mac"
         exit -1
    fi
    cat <<EOF >"${HOME}/Desktop/${app}.desktop"
[Desktop Entry]
    Exec=${ScriptDir}/update.sh -server "$server" -port "${port}"
    Encoding=UTF-8
    Icon=${ScriptDir}/icon.ico
    Name=${app}
    Path=${ScriptDir}
    Type=Application
EOF

}

if [ "${install}" == "true" ]; then
    Install
else
    Update
    Launch
fi

if [ "${wait}" == "true" ]; then
    echo -n "Type return to quit! "
    read a
fi
