package vrptwfl.metaheuristic.instanceGeneration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Helper class for loading hospital instances. 
 * It uses a GSONBuilder to get instances of the loaded information. 
 * The loaded information is stored within an object of type ImportedInstance.
 * 
 * @author Alexander Jungwirth, Christian M.M. Frey
 */
public class HospitalGSONLoader {

	/**
	 * Load the hospital instance being attached as parameter.
	 * @param instanceName: name of hospital instance being loaded.
	 * @return loaded instance as instance of class ImportedInstance
	 * @throws IOException: occurs if file could not be read
	 */
	public ImportedInstance loadHospitalInstanceFromJSON(String instanceName) throws IOException {
		ImportedInstance instance;

		// get path to "InstancesHospital\\*InsertFileName*.json"
		// String path = GeneralUtil.getPathToDirectoryInstancesHospital() + File.separator + instanceName + ".json";
		String path = "./instances/Instances-Hospital/" + instanceName + ".json";

		// important to take FileInputStream and not InputStream
		FileInputStream stream = new FileInputStream(path);

		try(Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			Gson gson = new GsonBuilder().create();
			instance = gson.fromJson(reader, ImportedInstance.class);
		}
		return instance;
	}
}
