/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.controllers;

import com.seniorproject.semanticweb.services.WebServices;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
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
//        String result = this.webServices.queryJena(queryString);
        System.out.println("advanceSearch");
        String result = this.webServices.convertToJSON(queryString);
        System.out.println(result);
        return result;
    }

    @RequestMapping(value = "/facetedSearch", method = RequestMethod.GET)
    public @ResponseBody
    String facetedSearch(@RequestParam("searchString") String searchString,@RequestParam("type") String type) throws IOException, InterruptedException {
        System.out.println("facetedSearch");
        String queryString = this.webServices.genSparql(searchString,type);
        System.out.println(queryString);
//        String result = this.webServices.queryHadoop(queryString);
//        System.out.print(queryString);
//        String result = this.webServices.queryJena(queryString);
//        System.out.print(result);
        return "";
    }
}
