package com.example.controller;

import com.example.model.Product;
import com.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    private static final int[] PAGE_SIZES = {2, 5, 7, 10};

    @Autowired
    private ProductRepository repo;

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

    @RequestMapping("/")
    public String viewHomePage(Model model) {

        return listByPage(model, 1, 2, null, "id", "asc", null, null);

    }

    @GetMapping("/page/{pageNumber}")
    public String listByPage(Model model, @PathVariable("pageNumber") int currentPage, @RequestParam("pageSize") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "sortField1", required = false) String sortField1, @RequestParam(value = "sortDir1", required = false) String sortDir1, @RequestParam(value = "sortField2", required = false) String sortField2, @RequestParam(value = "sortDir2", required = false) String sortDir2) {

        List<Sort.Order> orders = new ArrayList<>();

        if (sortField1 != null && !("").equals(sortField1) && !("null").equals(sortField1)) {
            orders.add(new Sort.Order(getSortDirection(sortDir1), sortField1));
        }

        if (sortField2 != null && !("").equals(sortField2) && !("null").equals(sortField2)) {

            orders.add(new Sort.Order(getSortDirection(sortDir2), sortField2));
        }

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(orders));

        Page<Product> page;
        if (keyword != null) {
            //page = repo.findAllByNameContainingOrBrandContainingOrMadeinContaining(keyword, keyword, keyword, pageable);
            page = repo.findAllByContaining(keyword, pageable);
        } else {
            page = repo.findAll(pageable);
        }

        long totalItems = page.getTotalElements();
        int totalPages = page.getTotalPages();
        int numberOfElements = page.getNumberOfElements();

        List<Product> listProducts = page.getContent();

        model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sortField1", sortField1);
        model.addAttribute("sortDir1", sortDir1);
        model.addAttribute("sortField2", sortField2);
        model.addAttribute("sortDir2", sortDir2);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("numberOfElements", numberOfElements);
        model.addAttribute("listProducts", listProducts); // next bc of thymeleaf we make the index.html

        model.addAttribute("keyword", keyword);

        return "index";
    }

    @RequestMapping("/new")
    public String showNewProductForm(Model model
    ) {
        Product product = new Product();
        model.addAttribute("product", product);

        return "new_product";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveProduct(@ModelAttribute("product") Product product
    ) {
        repo.save(product);

        return "redirect:/";
    }

    @RequestMapping("/edit/{id}")
    public ModelAndView showEditProductForm(@PathVariable(name = "id") Long id
    ) {
        ModelAndView modelAndView = new ModelAndView("edit_product");
        Product product = repo.findById(id).orElse(null);
        modelAndView.addObject("product", product);

        return modelAndView;
    }

    @RequestMapping("/delete/{id}")
    public String deleteProduct(@PathVariable(name = "id") Long id
    ) {
        repo.deleteById(id);

        return "redirect:/";
    }

}
