package xcdh.MVC;

import org.yaml.snakeyaml.Yaml;
import xcdh.MVC.annotation.Autowired;
import xcdh.MVC.annotation.Controller;
import xcdh.MVC.annotation.RequestMapping;
import xcdh.MVC.annotation.Service;

import xcdh.Mybatis.SqlSession;
import xcdh.Mybatis.annotation.Mapper;
import xcdh.springboot.anotation.*;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebApplicationContext {
    private String beanDefinationLocation;

    private List<String> prefilelist = new ArrayList<>();
    private List<String> filelist = new ArrayList<>();
    private final Map<String, Object> ioC = new HashMap<>();

    private List<Handlerr> handlers = new ArrayList<>();

    private SqlSession session;
    public WebApplicationContext(String beanDefinationLocation) {
        this.beanDefinationLocation = beanDefinationLocation;
        session = new SqlSession();

    }

    public void refresh() {
        try {
            List<String> strings = JarPrase.readJar();
            filelist.addAll(strings);

            String replace = "/" + beanDefinationLocation.replace(".", "/");
            URL resource = WebApplicationContext.class.getResource(replace + ".class");
            String path = resource.getPath();
            File file = new File(path);
            String name = file.getParentFile().getName();

            URL resource1 = WebApplicationContext.class.getResource("/xcdh");
            String path1 = resource1.getPath();
            File file1 = new File(path1);
            String name2 = file1.getName();
            getAllFilePre(name2);
            getAllFile(name);

            portAndAutoWired(prefilelist);
            portAndAutoWired(filelist);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void portAndAutoWired(List<String> filelist) throws Exception {
        //对象注入容器中
        for (String s1 : filelist) {
            Class<?> aClass = Class.forName(s1);

            if (aClass.isAnnotationPresent(Controller.class)) {

                Object o = Instance_and_DI(aClass);
                Method[] methods = aClass.getDeclaredMethods();
                for (Method method : methods) {
                    RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                    String url = annotation.value();
                    handlers.add(new Handlerr(url, o, method));
                }

            }

            if (aClass.isAnnotationPresent(Service.class) || aClass.isAnnotationPresent(Component.class)) {

                if (aClass.getSimpleName().equals("SqlSession")) {
                    Instance_and_DI(aClass,session);
                } else {
                    Instance_and_DI(aClass);
                }
            }

            if (aClass.isAnnotationPresent(Configuration.class)) {
                Instance_and_DI(aClass);
                putBeanAnotation(aClass);
            }

            if (aClass.isAnnotationPresent(Import.class)) {
                Import annotation = aClass.getAnnotation(Import.class);
                Class<?> clazz = annotation.value();
                if (ioC.get(clazz.getSimpleName())==null) {
                    ioC.put(clazz.getSimpleName(), clazz.newInstance());
                }
            }

            if (aClass.isAnnotationPresent(ConfigutationProperties.class)) {
                ConfigutationProperties annotation = aClass.getAnnotation(ConfigutationProperties.class);
                String prefix = annotation.value();

                HashMap map;
                Yaml yaml = new Yaml();
                try {
                    if (WebApplicationContext.class.getClassLoader().getResourceAsStream("application.yaml") != null) {
                        InputStream in = WebApplicationContext.class.getClassLoader().getResourceAsStream("application.yaml");
                        map = yaml.loadAs(in, HashMap.class);
                    } else {
                        InputStream in = WebApplicationContext.class.getClassLoader().getResourceAsStream("application.yml");
                        map = yaml.loadAs(in, HashMap.class);
                    }
                    if (ioC.get(aClass.getSimpleName())==null) {
                        ioC.put(aClass.getSimpleName(), aClass.newInstance());
                    }
                    Object o = ioC.get(aClass.getSimpleName());
                    Field[] f = aClass.getDeclaredFields();
                    for (Field field : f) {
                        field.setAccessible(true);
                        Map mp = (Map) map.get(prefix);
                        String value = (String) mp.get(field.getName());
                        field.set(o, value);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (aClass.isInterface() && aClass.isAnnotationPresent(Mapper.class)) {
                Object proxy = session.getProxy(aClass);
                ioC.put(aClass.getSimpleName(), proxy);
            }
        }

    }

    private void putBeanAnotation(Class<?> aClass) throws Exception {
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(Bean.class)) {
                Class<?> returnType = method.getReturnType();
                Constructor<?> constructor = returnType.getDeclaredConstructor(method.getParameterTypes());
                Object o = constructor.newInstance();
                ioC.put(returnType.getSimpleName(), o);
            }

        }
    }

    private boolean hasAutoWired(Class<?> aClass) {
        for (Field field : aClass.getDeclaredFields()) {
            boolean b = field.isAnnotationPresent(Autowired.class);
            if(b){
                return b;
            }
        }
        return false;
    }

    private Object Instance_and_DI(Class<?> aClass,Object o3) throws Exception{
        if (aClass.isInterface() && aClass.isAnnotationPresent(Mapper.class)) {
            Object proxy = session.getProxy(aClass);
            ioC.put(aClass.getSimpleName(), proxy);
            return proxy;
        }
        if (ioC.get(aClass.getSimpleName())!=null) {
            return ioC.get(aClass.getSimpleName());
        }

        if(hasAutoWired(aClass)){
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class )) {
                    Object o2=Instance_and_DI(field.getType());
                    field.setAccessible(true);
                    field.set(o3,o2);
                }
            }
        }
        ioC.put(aClass.getSimpleName(),o3);
        return o3;
    }
    private Object Instance_and_DI(Class<?> aClass) throws Exception{
        if (aClass.isInterface() && aClass.isAnnotationPresent(Mapper.class)) {
            Object proxy = session.getProxy(aClass);
            ioC.put(aClass.getSimpleName(), proxy);
            return proxy;
        }


        if (ioC.get(aClass.getSimpleName())!=null) {
            return ioC.get(aClass.getSimpleName());
        }
        Object o1 = aClass.newInstance();
        if(hasAutoWired(aClass)){
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class )) {
                    Object o2=Instance_and_DI(field.getType());
                    field.setAccessible(true);
                    field.set(o1,o2);
                }
            }
        }
        ioC.put(aClass.getSimpleName(),o1);
        return o1;
    }


    private  void getAllFile(String s1) {
        String replace = "/"+s1.replace(".", "/");
        File file = new File( WebApplicationContext.class.getResource(replace).getPath());
        File[] files = file.listFiles();
        for (File file1 : files) {
            String name=s1+"."+file1.getName();

            if (file1.isDirectory()) {
                getAllFile(name);   //这里 组装的非常巧妙！！
            }else {
                filelist.add(name.replace(".class",""));
            }

        }
    }

    private  void getAllFilePre(String s1) {
        String replace = "/"+s1.replace(".", "/");
        File file = new File( WebApplicationContext.class.getResource(replace).getPath());
        File[] files = file.listFiles();
        for (File file1 : files) {
            String name=s1+"."+file1.getName();

            if (file1.isDirectory()) {
                getAllFile(name);   //这里 组装的非常巧妙！！
            }else {
                prefilelist.add(name.replace(".class",""));
            }



        }
    }

    public List<Handlerr> getHandlers() {
        return handlers;
    }

}