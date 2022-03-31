package vrptwfl.metaheuristic.instanceGeneration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HospitalGSONLoader {

	ImportedInstance loadHospitalInstanceFromJSON(String instanceName) throws IOException {

		ImportedInstance instance;

		// get path to "InstancesHospital\\*InsertFileName*.json"
		// String path = GeneralUtil.getPathToDirectoryInstancesHospital() + File.separator + instanceName + ".json";
		String path = "./instances/Instances-Hospital/" + instanceName + ".json";
		System.out.println(path);

		// important to take FileInputStream and not InputStream
		FileInputStream stream = new FileInputStream(path);

		try(Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			Gson gson = new GsonBuilder().create();
			instance = gson.fromJson(reader, ImportedInstance.class);
		}
		
		return instance;
	}

}
