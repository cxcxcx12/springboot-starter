package xcdh.Mybatis;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import xcdh.MVC.annotation.Autowired;
import xcdh.springboot.anotation.Component;
import xcdh.springboot.anotation.ConfigutationProperties;
import xcdh.springboot.anotation.Import;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Import(configProper.class)
public class Configuration {
    String driver;
    String url;
    String username;
    String password;
    Statement statement;

    @Autowired
    configProper co;



    public List<String> getMapperUrls() {
        return mapperUrls;
    }

    public void setMapperUrls(List<String> mapperUrls) {
        this.mapperUrls = mapperUrls;
    }


    Map<String,sqlMapping> mapping;
     List<String> mapperUrls=new ArrayList<>();


    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, sqlMapping> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, sqlMapping> mapping) {
        this.mapping = mapping;
    }


    public Statement  getConnection(){
        driver=co.driver;
        url=co.url;
        password=co.password;
        username=co.username;
        Statement statement = null;
        Connection conn = null;     // sql语句，
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(getUrl(), getUsername(), getPassword());

            return conn.createStatement();
    }catch (Exception e){
            e.printStackTrace();
        }
      return null;
    }




}
