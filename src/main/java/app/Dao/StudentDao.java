package app.Dao;

import app.pojo.Student;
import xcdh.Mybatis.annotation.Mapper;
import xcdh.Mybatis.annotation.Param;

import java.util.List;


@Mapper
public interface StudentDao {


    void insertuser(@Param("name")String name, @Param("id")String id, @Param("age")String age);
    List<Student> find(@Param("name")String name, @Param("age")String age);

}
