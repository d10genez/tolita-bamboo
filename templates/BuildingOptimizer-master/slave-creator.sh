#! bin/bash

for (( i = 0; i < $1; i++ )); do
	c=$2"-"$i
	gcutil --service_version="v1" --project="plated-reducer-359" adddisk "$c" --zone="us-central1-a" --source_snapshot="slave-march-31"
	gcutil --service_version="v1" --project="plated-reducer-359" addinstance "$c" --zone="us-central1-a" --machine_type="n1-standard-1" --network="default" --external_ip_address="ephemeral" --service_account_scopes="https://www.googleapis.com/auth/userinfo.email,https://www.googleapis.com/auth/compute,https://www.googleapis.com/auth/devstorage.full_control" --disk="$c,deviceName=$c,mode=READ_WRITE,boot" --metadata=startup-script:'#! /bin/bash
	git clone https://github.com/dibyom/BuildingOptimizer.git
	cd BuildingOptimizer
	ant slave -Dargs="-file params/building.slave.params" >> output.log
	EOF'
done