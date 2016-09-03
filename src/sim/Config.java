/*
 * Copyright (c) 2010, James Hanlon
 * All rights reserved.
 * 
 * Made available under the BSD license - see the LICENSE file
 */ 
package sim;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Config {

	public static final String RESULTS_DIR = "results";
	
	// Enumerations
	public enum Mode {
		DEBUG,
		VISUALISE,
		RUN
	}
	public enum TopologyType { 
		MESH,
		TORUS,
		DEGENMESH,
		DEGENTORUS,
	};
	public enum RoutingType {
		MINIMAL,
		DOR,
		UPDOWN,
	};
	public enum TrafficType {
		UNIFORM,
		BITCOMP,
		BITREV,
		TRANSPOSE,
		SHUFFLE,
		TORNADO,
		NEIGHBOUR,
		RANDPERM,
		DIAGONAL,
		ASYMMETRIC,
		TRACE
	}
	public enum InjProcess {
		BERNOULLI,
		ONOFF,
		TRACE
	}
	
	// Parameter map
	private static Map<String, Object> m_params;
	private static String              m_date;
	private Config() {}
	
	public static void init(String filename) {
		
		m_params = new HashMap<String, Object>();
		setDefaultParams();
		
		// Set file suffix
		Format formatter = new SimpleDateFormat("[ss-mm-HH-dd-MM]");
		m_date = formatter.format(new Date());
		
		// Load the configuration file into a properties object
		Properties properties = new Properties();
	    try {
	        properties.load(new FileInputStream(filename));
	    } catch (IOException e) {
	    	System.err.println("Error: could not read config file "+filename);
	    }
	    
	    // Read the simulation type and initialise corresponding parameters
	    Mode mode = Mode.valueOf(properties.getProperty("mode").toUpperCase());
	    setParam("mode", mode);
	    
	    try {
		    switch(mode) {
		    case RUN:
		    	readTopologyParams(properties);
		    	readRoutingParams(properties);
		    	readTrafficParams(properties);
		    	readNetworkParams(properties);
		    	readRunSimParams(properties);
		    	break;
		    case DEBUG:
		    	readTopologyParams(properties);
		    	readRoutingParams(properties);
		    	readTrafficParams(properties);
		    	readNetworkParams(properties);
		    	readDebugSimParams(properties);
		    	break;
		    case VISUALISE:
		    	readTopologyParams(properties);
		    	break;
		    default:
		    	System.err.println("Error: no run mode found in config");
		    }
	    } catch (NullPointerException e1) {
	    	System.err.println("Error: missing parameter "+e1.getMessage());
	    	System.exit(1);
		} catch (NumberFormatException e2) {
			System.err.println("Error: invalid parameter "+e2.getMessage());
			e2.printStackTrace();
			System.exit(1);
		} catch (Exception e3) {
			System.err.println("Error: "+e3.getMessage()+" "+e3);
			System.exit(1);
		}
	    
	    System.out.println("Read configuration file "+filename+"\n");
	    //System.out.println(dumpConfig());
	}

	private static void readTopologyParams(Properties properties)
	throws NumberFormatException, Exception {
		if(!properties.containsKey("topology"))
			throw new Exception("invalid 'topology' key");
		
		TopologyType topology = TopologyType.valueOf(properties.getProperty("topology").toUpperCase());
		setParam("topology", topology);
		
		switch(topology) {
		case MESH:
		case TORUS:
			getIntProperty(properties, "n");
			getIntProperty(properties, "k");
			break;
			
		case DEGENMESH:
		case DEGENTORUS:
			getIntProperty(properties, "n");
			getIntProperty(properties, "k");
			getDubProperty(properties, "faults");
			break;
		
		default:
			throw new Exception("invalid topology");
		}
	}

	private static void readRoutingParams(Properties properties)
	throws Exception {
		
		if(properties.containsKey("routing")) {
			
			RoutingType routing = RoutingType.valueOf(
					properties.getProperty("routing").toUpperCase());
			setParam("routing", routing);
			
			switch(routing) {
			case UPDOWN:
				getIntProperty(properties, "root_node");
				break;
			}
			
		} else {
			throw new Exception("no 'routing' key");
		}
	}
	
	private static void readTrafficParams(Properties properties)
	throws NumberFormatException, Exception {
		
		// Get the traffic type and specific parameters
		if(properties.containsKey("traffic_pattern")) {
			TrafficType traffic = TrafficType.valueOf(properties.getProperty("traffic_pattern").toUpperCase());
			setParam("traffic_pattern", traffic);
			switch(traffic) {
			case UNIFORM:
			case BITCOMP:
			case BITREV:
			case TRANSPOSE:
			case SHUFFLE:
			case TORNADO:
			case NEIGHBOUR:
			case DIAGONAL:
			case ASYMMETRIC:
				break;
			
			case RANDPERM:
				getSeedProperty(properties, "perm_seed");
				break;
			
			case TRACE:
				setParam("injection_type", InjProcess.TRACE);
				getStrProperty(properties, "trace_file");
				break;
			
			default:
				throw new Exception("invalid 'traffic_pattern' key value");
			}
		} else {
			throw new Exception("no 'traffic_pattern' key");
		}
		
		// Get the injection type and any specific params
		if(properties.containsKey("injection_process")) {
			InjProcess injection = InjProcess.valueOf(properties.getProperty("injection_process").toUpperCase());
			setParam("injection_process", injection);
			switch(injection) {
			case BERNOULLI:
				getDubProperty(properties, "injection_rate");
				getDubProperty(properties, "injection_step");
				break;
				
			case ONOFF:
				getDubProperty(properties, "burst_alpha");
				getDubProperty(properties, "burst_beta");
				if(getDubParam("burst_alpha") + getDubParam("burst_beta") != 1)
					throw new Exception("Invalid alpha and beta burst values");
				getDubProperty(properties, "injection_rate");
				getDubProperty(properties, "injection_step");
				break;

			default:
				throw new Exception("invalid 'injection_type' key value");
			}
		} else {
			throw new Exception("no 'injection_type' key");
		}
		
		// Get other traffic parameters
		getIntProperty(properties, "flits_per_packet");
	}
	
	private static void readNetworkParams(Properties properties) 
	throws NumberFormatException, Exception {
		getIntProperty(properties,  "buffer_size");
		getIntProperty(properties,  "num_vcs");
		getIntProperty(properties,  "available_vcs");
		getIntProperty(properties,  "buffer_size");
		getIntProperty(properties,  "link_delay");
		getSeedProperty(properties, "rand_seed");
	}

	private static void readDebugSimParams(Properties properties) 
	throws NumberFormatException, Exception {
		getIntProperty(properties, "max_msgs");
		getIntProperty(properties, "max_cycles");
	}

	private static void readRunSimParams(Properties properties)
	throws NumberFormatException, Exception {
		getIntProperty(properties, "sim_runs");
		getIntProperty(properties, "sample_period");
		getIntProperty(properties, "num_samples");
		getIntProperty(properties, "warmup_period");
		getIntProperty(properties, "latency_thresh");
		getDubProperty(properties, "warmup_thresh");
	}
	
	public static void setDefaultParams() {
		// Mode
		setParam("mode",              Mode.RUN);
		
		// Topology
		setParam("topology",          TopologyType.MESH);
		setParam("k",                 8);
		setParam("n",                 8);
		setParam("d",                 6);
		setParam("p",                 0.05);
		setParam("m",                 4);
		setParam("faults",            0.05);
		setParam("steps",             60);
		setParam("num_nodes",         128);
		setParam("graph_seed",        new Long(0));
		setParam("root_node",         0);
		
		// Routing
		setParam("routing",           RoutingType.UPDOWN);
		
		// Network
		setParam("num_vcs",           3);
		setParam("available_vcs",     3);
		setParam("buffer_size",       5);
		setParam("link_delay",        4);
		
		// Simulation
		setParam("max_msgs",          1000);
		setParam("max_cycles",        1000);
		setParam("rand_seed",         System.currentTimeMillis());
		setParam("max_msgs",          1000);
		setParam("sim_runs",          1);
		setParam("sample_period",     1000);
		setParam("num_samples",       100);
		setParam("warmup_period",     0);
		setParam("latency_thresh",    4000);
		setParam("warmup_thresh",     0.05);
		
		// Traffic
		setParam("traffic_pattern",   TrafficType.UNIFORM);
		setParam("flits_per_packet",  20);
		setParam("trace_file",        "");
		setParam("injection_rate",    0.1);
		setParam("injection_step",    0.01);
		setParam("injection_process", InjProcess.BERNOULLI);
		setParam("burst_alpha",       0.2);
		setParam("burst_beta",        0.8);
		setParam("perm_seed",         new Long(0));
	}
	
	private static void getIntProperty(Properties properties, String key) 
	throws NumberFormatException {
		if(!m_params.containsKey(key))
			System.err.println("Error: missing key "+key);
		if(properties.containsKey(key))
			m_params.put(key, Integer.parseInt(properties.getProperty(key)));
	}
	
	private static void getDubProperty(Properties properties, String key) 
	throws NumberFormatException {
		if(!m_params.containsKey(key))
			System.err.println("Error: missing key "+key);
		if(properties.containsKey(key))
			m_params.put(key, Double.parseDouble(properties.getProperty(key)));
	}
	
	private static void getStrProperty(Properties properties, String key) {
		if(!m_params.containsKey(key))
			System.err.println("Error: missing key "+key);
		if(properties.containsKey(key))
			m_params.put(key, properties.getProperty(key));
	}
	
	private static void getSeedProperty(Properties properties, String key) {
		if(properties.containsKey(key)) {
			String seed = properties.getProperty(key);
			setParam(key, new Long(seed.toLowerCase().equals("time") ? 
					System.currentTimeMillis() : Long.parseLong(seed)));
		}
	}
	
	private static void setParam(String key, Object value) {
		m_params.put(key, value);
	}
	
	private static int getIntParam(String key) {
		return ((Integer) m_params.get(key)).intValue();
	}
	
	private static double getDubParam(String key) {
		return ((Double) m_params.get(key)).doubleValue();
	}
	
	private static long getLngParam(String key) {
		return ((Long) m_params.get(key)).longValue();
	}
	
	private static String getStrParam(String key) {
		return (String) m_params.get(key);
	}
	
	public static void setArgParam(String key, String value) {
		
		if(key.equals("traffic_pattern")) {
			setParam(key, TrafficType.valueOf(value.toUpperCase()));
		}
		
		else if(key.equals("routing")) {
			setParam(key, RoutingType.valueOf(value.toUpperCase()));
		}
		
		else if(key.equals("faults")) {
			setParam(key, Double.parseDouble(value));
		}
		
		else if(key.equals("num_nodes")) {
			setParam(key, Integer.parseInt(value));
		}
		
		else if(key.equals("p")) {
			setParam(key, Double.parseDouble(value));
		}
		
		else if(key.equals("d")) {
			setParam(key, Integer.parseInt(value));
		}
		
		else if(key.equals("m")) {
			setParam(key, Integer.parseInt(value));
		}
		
		else if(key.equals("steps")) {
			setParam(key, Integer.parseInt(value));
		}
	}
	
	public static void setRouting(RoutingType routing) {
		setParam("routing", routing);
	}
	
	public static String dumpConfig() {
		switch(mode()) {
		case RUN:        return dumpRunConfig();
		case DEBUG:      return dumpDebugConfig();
		case VISUALISE:  return dumpVisConfig();
		default:         return null;
		}
	}

	private static String dumpRunConfig() {
		String s = "";
		s += "  Mode:                    "+mode()+"\n";
		s += "[TOPOLOGY]=========================================\n";
		s += "  Topology (k,n):          "+topology()+" ("+k()+","+n()+")\n";
		s += "  Routing algorithm:       "+routing()+"\n";
		s += "[NETWORK]==========================================\n";
		s += "  Num virtual channels:    "+numVCs()+"\n";
		s += "  Buffer size:             "+bufferSize()+"\n";
		s += "  Link delay:              "+linkDelay()+"\n";
		s += "[SIMULATION]=======================================\n";
		s += "  Random seed:             "+seed()+"\n";
		s += "  Num simulation runs      "+simRuns()+"\n";
		s += "  Sample period (cycles):  "+samplePeriod()+"\n";
		s += "  Num samples:             "+numSamples()+"\n";
		s += "  Latency threshold:       "+latencyThresh()+"\n";
		s += "  Warmup threshold:        "+warmupThresh()+"\n";
		s += "[TRAFFIC]==========================================\n";
		s += "  Traffic type:            "+traffic()+"\n";
		s += "  Perm seed:               "+permSeed()+"\n";
		s += "  Injection type:          "+injection()+"\n"; 
		s += "  Burst alpha:             "+burstAlpha()+"\n";
		s += "  Burst beta:              "+burstBeta()+"\n";
		s += "  Packet injection rate:   "+injectionRate()+"\n";
		s += "  Const flits per packet:  "+packetSize()+"\n";
		s += "---------------------------------------------------\n";
		return s;
	}
	
	public static String dumpExpConfig() {
		String s = "";
		s += "  Mode:                    "+mode()+"\n";
		s += "[TOPOLOGY]=========================================\n";
		s += "  Topology (k,n):          "+topology()+" ("+k()+","+n()+")\n";
		s += "  Num layers:              "+numLayers()+"\n";
		s += "  Seg seed:                "+segSeed()+"\n";
		s += "[NETWORK]==========================================\n";
		s += "  Num virtual channels:    "+numVCs()+"\n";
		s += "  Num aval. vc:            "+availableVCs()+"\n";
		s += "[SIMULATION]=======================================\n";
		s += "  Random seed:             "+seed()+"\n";
		s += "[TRAFFIC]==========================================\n";
		s += "  Traffic type:            "+traffic()+"\n";
		s += "  Perm seed:               "+permSeed()+"\n";
		s += "  Packet injection step:   "+injectionStep()+"\n";
		s += "  Const flits per packet:  "+packetSize()+"\n";
		s += "---------------------------------------------------\n";
		return s;
	}

	private static String dumpVisConfig() {
		String s = "";
		s += "[TOPOLOGY]=========================================\n";
		s += "  Topology (k,n):          "+topology()+" ("+k()+","+n()+")\n";
		s += "  p:                       "+p()+"\n";
		s += "  d:                       "+d()+"\n";
		s += "  m:                       "+m()+"\n";
		s += "  Num nodes:               "+numNodes()+"\n";
		s += "  Steps:                   "+steps()+"\n";
		s += "  Random seed:             "+graphSeed()+"\n";
		s += "---------------------------------------------------\n";
		return s;
	}
	
	private static String dumpDebugConfig() {
		String s = "";
		Set<String> keys = m_params.keySet();
		for(String key : keys) {
			s += String.format("%-20s %-10s", key, m_params.get(key))+"\n";
		}
		return s;
	}
	
	public static Mode         mode()           { return (Mode) m_params.get("mode"); }
	public static boolean      debugMode()      { return mode().equals(Mode.DEBUG); }
	public static boolean      runMode()        { return mode().equals(Mode.RUN); }
	public static TopologyType topology()       { return (TopologyType) m_params.get("topology"); }
	public static RoutingType  routing()        { return (RoutingType) m_params.get("routing"); }
	public static TrafficType  traffic()        { return (TrafficType) m_params.get("traffic_pattern"); }
	public static InjProcess   injection()      { return (InjProcess) m_params.get("injection_process"); }
	public static int          k()              { return getIntParam("k"); }
	public static int          n()              { return getIntParam("n"); }
	public static int          d()              { return getIntParam("d"); }
	public static double       p()              { return getDubParam("p"); }
	public static int          m()              { return getIntParam("m"); }
	public static double       faults()         { return getDubParam("faults"); }
	public static int          steps()          { return getIntParam("steps"); }
	public static int          numNodes()       { return getIntParam("num_nodes"); }
	public static String       traceFile()      { return getStrParam("trace_file"); }
	public static double       injectionRate()  { return getDubParam("injection_rate"); }
	public static double       injectionStep()  { return getDubParam("injection_step"); }
	public static int          bufferSize()     { return getIntParam("buffer_size"); }
	public static int          numVCs()         { return getIntParam("num_vcs"); }
	public static int          availableVCs()   { return getIntParam("available_vcs"); }
	public static int          maxCycles()      { return getIntParam("max_cycles"); }
	public static int          maxMsgs()        { return getIntParam("max_msgs"); }
	public static int          warmupPeriod()   { return getIntParam("warmup_period"); }
	public static int          linkDelay()      { return getIntParam("link_delay"); }
	public static int          packetSize()     { return getIntParam("flits_per_packet"); }
	public static long         seed()           { return getLngParam("rand_seed"); }
	public static int          samplePeriod()   { return getIntParam("sample_period"); }
	public static int          numSamples()     { return getIntParam("num_samples"); }
	public static int          latencyThresh()  { return getIntParam("latency_thresh"); }
	public static int          simRuns()        { return getIntParam("sim_runs"); }
	public static double       burstAlpha()     { return getDubParam("burst_alpha"); }
	public static double       burstBeta()      { return getDubParam("burst_beta"); }
	public static long         permSeed()       { return getLngParam("perm_seed"); }
	public static double       warmupThresh()   { return getDubParam("warmup_thresh"); }
	public static void         setInjRate(double rate) { setParam("injection_rate", rate); }
	public static void         setInjStep(double step) { setParam("injection_step", step); }
	public static int          numLayers()      { return getIntParam("num_layers"); }
	public static long         graphSeed()      { return getLngParam("graph_seed"); }
	public static long         segSeed()        { return getLngParam("seg_seed"); }
	public static String       date()           { return m_date; }
	public static void         setLayers(int layers) { setParam("num_layers", layers); }
	public static void         setAvailableVCs(int vcs) { setParam("available_vcs", vcs); }
	public static String routingName() {
		switch(Config.routing()) {
		case DOR:     return "DOR";
		case UPDOWN:  return "UD";
		}
		return null;
	}
}
