/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seniorproject.semanticweb.controllers;

import com.seniorproject.semanticweb.services.WebServices;
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
        model.addAttribute("searchForm", new SearchForm());
        return "index";
    }

//    @RequestMapping(value = "/query", method = RequestMethod.POST)
//    public String query(@ModelAttribute("queryString") SearchForm searchForm, Model model) {
//       String result = this.webServices.queryJena(searchForm.getQueryString());
//       model.addAttribute("result", result);
//       return "result";
//    }
    @RequestMapping("/advanceSearch")
    public @ResponseBody
    String advanceSearch(@RequestParam(value = "searchString") String queryString) {
//        String result = this.webServices.queryJena(queryString);
        System.out.println("controller");
        String result = this.webServices.convertToJSON(queryString);
        return result;
    }

    @RequestMapping("/facetedSearch")
    public @ResponseBody
    String facetedSearch(HttpServletRequest request, HttpServletResponse response) {
        System.out.println(request.getParameter("searchString"));
        System.out.println(request.getParameter("type"));
        String queryString = this.webServices.genSparql(request.getParameter("searchString"),request.getParameter("type"));
        System.out.print(queryString);
        String result = this.webServices.queryJena(queryString);
        System.out.print(result);
        return result;
    }
}
