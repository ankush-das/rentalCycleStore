package com.practice.cycle.Cycledemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.PostConstruct;

@Controller
public class CycleController {

	@Autowired
	private CycleRepository cycleRepo;

	private List<Cycle> cycleList;
	private List<Cycle> borrowedCycleList;
	private List<Cycle> availablecycleList;

	@PostConstruct
	public void init() {
		cycleList = new ArrayList<>();
		borrowedCycleList = new ArrayList<>();
		availablecycleList = new ArrayList<>();
		cycleRepo.findAll().forEach(c -> cycleList.add(c));
	}

	@GetMapping("/cycle")
	public String allcycleList(Model model) {
		model.addAttribute("cycleList", cycleList);
		return "rentalCycleShop";
	}

	@PostMapping("/addCycle")
	public String addCycle(@RequestParam String brand, @RequestParam String color) {
		Cycle newCycle = new Cycle();
		newCycle.setBrand(brand);
		newCycle.setColor(color);
		newCycle.setAvailable(true);

		cycleRepo.save(newCycle);
		init();

		return "redirect:/cycle";
	}

	@PostMapping("/rent/{id}")
	public String rentCycle(@PathVariable int id) {
		Cycle cycle = cycleRepo.findById(id);
		System.out.println(cycle);
		if (cycle != null) {
			cycle.setAvailable(false);
			borrowedCycleList.add(cycle);
			availablecycleList.remove(cycle);
			cycleRepo.save(cycle);
		}
		return "redirect:/cycle";
	}

	@GetMapping("/borrowedcycle")
	public String borrowedcycleList(Model model) {
		borrowedCycleList = cycleRepo.findByAvailable(false);
		model.addAttribute("borrowedCycleList", borrowedCycleList);
		return "borrowedCyclePage";
	}

	@PostMapping("/borrowedcycle/{id}")
	public String borrowedcycleListPost(@PathVariable int id) {
		Cycle cycle = cycleRepo.findById(id);
		if (cycle != null) {
			cycle.setAvailable(true);
			borrowedCycleList.remove(cycle);
			availablecycleList.add(cycle);
			cycleRepo.save(cycle);
		}
		return "redirect:/borrowedcycle";
	}

	@GetMapping("/availablecycle")
	public String availablecycleList(Model model) {
		availablecycleList = cycleRepo.findByAvailable(true);
		model.addAttribute("availablecycleList", availablecycleList);
		return "returnedCycle";
	}

	@GetMapping("/restock")
	public String cycleStock(Model model) {
		// Calculate the count of duplicate cycles
		Map<String, Map<String, Long>> cycleCountMap = cycleList.stream()
				.collect(Collectors.groupingBy(cycle -> (String) cycle.getBrand(),
						Collectors.groupingBy(cycle -> (String) cycle.getColor(), Collectors.counting())));

		Map<String, Map<String, Long>> BcycleCountMap = borrowedCycleList.stream()
				.collect(Collectors.groupingBy(cycle -> (String) cycle.getBrand(),
						Collectors.groupingBy(cycle -> (String) cycle.getColor(), Collectors.counting())));

		Map<String, Map<String, Long>> AcycleCountMap = availablecycleList.stream()
				.collect(Collectors.groupingBy(cycle -> (String) cycle.getBrand(),
						Collectors.groupingBy(cycle -> (String) cycle.getColor(), Collectors.counting())));

		model.addAttribute("cycleCountMap", cycleCountMap);
		model.addAttribute("BcycleCountMap", BcycleCountMap);
		model.addAttribute("AcycleCountMap", AcycleCountMap);
		
		return "cycleStockInfo";
	}
}
