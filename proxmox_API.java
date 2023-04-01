//Proxmox virtualization(API)
PROXMOX_NODE_IP=192.168.1.254
PROXMOX_NODE_NAME=pve01
PROXMOX_STORAGE="Storage1"

API_USER=api
API_USER_PASSWORD=

CRED="username=$API_USER@pve&password=$API_USER_PASSWORD"

curl --silent --insecure --data $CRED https://$PROXMOX_NODE_IP:8006/api2/json/access/ticket | jq --raw-output '.data.ticket' | sed 's/^/PVEAuthCookie=/' > cookie
curl --silent --insecure --data $CRED https://$PROXMOX_NODE_IP:8006/api2/json/access/ticket | jq --raw-output '.data.CSRFPreventionToken' | sed 's/^/CSRFPreventionToken:/' > token

# container config

CPU=1
CPUUNITS=512
MEMORY=1024
DISK=15g
SWAP=2048
OS_TEMPLATE="Storage1:vztmpl/debian-10-standard_10.7-1_amd64.tar.gz"

# script options

case $1 in

    start|stop) curl --silent --insecure  --cookie "$(<cookie)" --header "$(<token)" -X POST https://$PROXMOX_NODE_IP:8006/api2/json/nodes/$PROXMOX_NODE_NAME/lxc/$2/status/$1; echo "  done." ;;

    create) curl --insecure  --cookie "$(<cookie)" --header "$(<token)" -X POST --data-urlencode net0="name=tnet$2,bridge=vmbr0" --data ostemplate=$OS_TEMPLATE --data storage=$PROXMOX_STORAGE --data vmid=$2 --data cores=$CPU --data cpuunits=$CPUUNITS --data mem>

    delete) curl --silent --insecure  --cookie "$(<cookie)" --header "$(<token)" -X DELETE https://$PROXMOX_NODE_IP:8006/api2/json/nodes/$PROXMOX_NODE_NAME/lxc/$2;echo "  done." ;;

    *) echo ""; echo " usage:  start|stop|create|delete <vmid> ";echo ""; ;;

esac

# remove cookie and token

rm cookie token