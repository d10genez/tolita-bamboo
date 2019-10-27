BuildingOptimizer
=================

NSGA-II based approach to optimizing building design for optimal energy efficiency

To run:

```
    ant run -Dargs="-file params/building.params"
```
Using Google Cloud Engine :

* Create a master using the master snapshot
* Note IP address of master and put it in building.master.params or building.slave.params
* push changes to the repo
* Start the master instance :
```
	gcutil ssh "master" 
	ant run -Dargs="-file params/building.master.params"
```
* To create and run masters (ensure IP address of master is correct)
``` 
	source slave-creator.sh
```
* Profit

*Output will be in out.stat and front.stat on master home*