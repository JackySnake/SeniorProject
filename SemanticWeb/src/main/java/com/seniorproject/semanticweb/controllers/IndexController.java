/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.controllers;

import com.seniorproject.semanticweb.services.WebServices;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author mtmmoei
 */
@Controller
public class IndexController {
    @Autowired
    ServletContext servletContext;
    
    private WebServices webServices;

    @Autowired
    public void setWebServices(WebServices webServices) {
        this.webServices = webServices;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("categories", this.webServices.getCategories());
        return "index";
    }

//    @RequestMapping(value = "/query", method = RequestMethod.POST)
//    public String query(@ModelAttribute("queryString") SearchForm searchForm, Model model) {
//       String result = this.webServices.queryJena(searchForm.getQueryString());
//       model.addAttribute("result", result);
//       return "result";
//    }
    @RequestMapping(value = "/advanceSearch", method = RequestMethod.GET)
    public @ResponseBody
    String advanceSearch(@RequestParam("searchString") String queryString) throws IOException, InterruptedException {
        System.out.println("advanceSearch");
        String filePath = this.webServices.queryHadoop(queryString);
      //  String filePath = servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/film_actor_data.txt");
        ArrayList<String> resultWithPrefix = this.webServices.replaceString(filePath);
        String result = this.webServices.convertToJSON(queryString,resultWithPrefix);
        
        return result;
    }
    
    @RequestMapping(value = "/selectCategory", method = RequestMethod.GET)
    public @ResponseBody
    ArrayList<String> selectCategory(@RequestParam("category") String category) {
        System.out.println("selectCategory");
        ArrayList<String> result = this.webServices.getProperties(category);
        return result;
    }
    
    @RequestMapping(value = "/addProperty", method = RequestMethod.GET)
    public @ResponseBody
    String addProperty(@RequestParam("category") String category,@RequestParam("property") String property,@RequestParam("selectedValues") String selectedValues) throws IOException, InterruptedException {
        System.out.println("addProperty");
        String queryString  = this.webServices.addPropertySparqlGenerator(category, property, selectedValues);
        System.out.println(queryString);
        
        String filePath = this.webServices.queryHadoop(queryString);
       

      //  String filePath = servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/personal_film_appearance_type.txt");
        System.out.println("hadoop done");
        ArrayList<String> resultWithPrefix = this.webServices.replaceString(filePath);
        //ArrayList<String> result = this.webServices.readFile(filePath);
        String result = this.webServices.countValue(resultWithPrefix);
        //System.out.println("result "+result);
        return result;
    }
     @RequestMapping(value = "/selectValue", method = RequestMethod.GET)
    public @ResponseBody
    String selectValue(@RequestParam("values") String json, @RequestParam("category") String category) throws IOException, InterruptedException {
        System.out.println("selectValue");
        String queryString  = this.webServices.selectValueSparqlGenerator(json,category);
        System.out.println(queryString);
        

        String filePath = this.webServices.queryHadoop(queryString);
     //   String filePath = servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/film_actor_lamour.txt");


        ArrayList<String> resultWithPrefix = this.webServices.replaceString(filePath);
        String result = this.webServices.readFileToJSON(resultWithPrefix, category);
      //  System.out.println(result);
        
        return result;
    }
    @RequestMapping(value = "/selectResult", method = RequestMethod.GET)
    public @ResponseBody
    String selectResult(@RequestParam("result") String json, @RequestParam("category") String category) throws IOException, InterruptedException {
        System.out.println("selectResult");
        String queryString  = this.webServices.selectResultSparqlGenerator(json,category);
        System.out.println(queryString);
        
        String filePath = this.webServices.queryHadoop(queryString);
      //  String filePath = servletContext.getRealPath("/WEB-INF/classes/PigSPARQL_v1.0/film_actor_data.txt");
        
        ArrayList<String> resultWithPrefix = this.webServices.replaceString(filePath);
        String result = this.webServices.readFileToJSON(resultWithPrefix,category);
        return result;
    }
    
}
