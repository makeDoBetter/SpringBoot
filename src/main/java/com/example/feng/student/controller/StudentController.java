package com.example.feng.student.controller;

import com.example.feng.annotation.FengRequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fengjirong
 * @date 2021/3/10 11:11
 */
@Controller
public class StudentController {

    @ResponseBody
    @FengRequestMapping(value = "/student", method = RequestMethod.GET)
    public String get(){
        return "get submit";
    }

    @ResponseBody
    @FengRequestMapping(value = "/student", method = RequestMethod.POST)
    public String post(){
        return "post submit";
    }

    @ResponseBody
    @FengRequestMapping(value = "/student", method = RequestMethod.PUT)
    public String put(){
        return "put submit";
    }

    @ResponseBody
    //@FengRequestMapping(value = "/student", method = RequestMethod.DELETE)
    @DeleteMapping(value = "/student")
    public String delete(){
        return "delete submit";
    }
}
