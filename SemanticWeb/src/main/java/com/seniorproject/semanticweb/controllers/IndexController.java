/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.controllers;

import com.seniorproject.semanticweb.services.HadoopService;
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
    private HadoopService hadoopService;

    @Autowired
    public void setWebServices(WebServices webServices) {
        this.webServices = webServices;
    }

    @Autowired
    public void setHadoopService(HadoopService hadoopService) {
        this.hadoopService = hadoopService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("categories", this.webServices.getCategories());
        return "index";
    }

    @RequestMapping(value = "/advanceSearch", method = RequestMethod.GET)
    public @ResponseBody
    String advanceSearch(@RequestParam("searchString") String queryString) throws IOException, InterruptedException {
        String filePath = this.hadoopService.queryHadoop(queryString);
        ArrayList<String> resultWithPrefix = this.webServices.replaceWithPrefix(filePath);
        String result = this.webServices.prepareAdvancedSearchResult(queryString, resultWithPrefix);
        return result;
    }

    @RequestMapping(value = "/selectCategory", method = RequestMethod.GET)
    public @ResponseBody
    ArrayList<String> selectCategory(@RequestParam("category") String category) {
        ArrayList<String> result = this.webServices.getProperties(category);
        return result;
    }

    @RequestMapping(value = "/addProperty", method = RequestMethod.GET)
    public @ResponseBody
    String addProperty(@RequestParam("category") String category, @RequestParam("property") String property, @RequestParam("selectedValues") String selectedValues) throws IOException, InterruptedException {
        String queryString = this.webServices.generateSparqlForAddProperty(category, property, selectedValues);
        String filePath = this.hadoopService.queryHadoop(queryString);
        ArrayList<String> resultWithPrefix = this.webServices.replaceWithPrefix(filePath);
        String result = this.webServices.prepareResultWithCount(resultWithPrefix);
        return result;
    }

    @RequestMapping(value = "/selectValue", method = RequestMethod.GET)
    public @ResponseBody
    String selectValue(@RequestParam("values") String json, @RequestParam("category") String category) throws IOException, InterruptedException {
        String queryString = this.webServices.generateSparqlForSelectValue(json, category);
        String filePath = this.hadoopService.queryHadoop(queryString);
        ArrayList<String> resultWithPrefix = this.webServices.replaceWithPrefix(filePath);
        String result = this.webServices.prepareFacetedSearchResult(resultWithPrefix, category);
        return result;
    }

    @RequestMapping(value = "/selectResult", method = RequestMethod.GET)
    public @ResponseBody
    String selectResult(@RequestParam("result") String json, @RequestParam("category") String category) throws IOException, InterruptedException {
        String queryString = this.webServices.generateSparqlForSelectResult(json);
        String filePath = this.hadoopService.queryHadoop(queryString);
        ArrayList<String> resultWithPrefix = this.webServices.replaceWithPrefix(filePath);
        String result = this.webServices.prepareResultDetail(resultWithPrefix, category);
        return result;
    }

}
