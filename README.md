# meshsim
Meshsim is simulator for mesh networks. It can calculate power consumption in tree-like mesh networks.

# Timing problem in networks
	 for tree-like mesh structures, it would be enough 
	 that ancestor nodes always turn on in time t + (n-1) * timeout
	 n is depth of their family
	 this way farthest nodes with no children will turn on in time 
	 t - timeout, their parent and uncles and aunts will turn in t
	 and then t + timeout and etc.
	 this way energy is conserved
	 additionally parent node could have children with different 
	 family depths 
	 in this situation parent node will have to activate twice to 
	 collect data, from both family lines
	 timeout should be time long enough for parent to collect data from all
	 children
	 generally parent could activate n different times 
	 generally node activates at least twice once to collect data, and once to propagate data
	 generally nodes have start numbers 
	 nodes activate when their start number is in queue
	 parent nodes activate with start number of their children first
	 after that they activate with their own start number 
	 generally time between activations is delta
	 gateway should calculate and send start configuration after 
	 probe configuration was acknowledged
	 however every node should only know the greatest start number because
	 the greatest start is one which dictates zero moment on start 
	 every other is calculated as (START_NUMBER - GREATEST) * DELTA...
