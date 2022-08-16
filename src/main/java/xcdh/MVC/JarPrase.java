package xcdh.MVC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class JarPrase {


    public static List<String> readJar() throws Exception {
        List<String> params= new ArrayList<>();
         ClassLoader classLoader = JarPrase.class.getClassLoader();
        Enumeration<URL> resources = classLoader.getResources("META-INF/spring.factory");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream(), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line=bufferedReader.readLine())!=null) {
                params.add(line);

            }

        }

          return params;

    }

    public static void main(String[] args) throws Exception {
        readJar();
    }


}


