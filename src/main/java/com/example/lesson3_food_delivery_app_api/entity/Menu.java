package com.example.lesson3_food_delivery_app_api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "menu_id", referencedColumnName = "id")
    private List<Food> foods;

    @OneToOne(mappedBy = "menu")
    private Restaurant restaurant;
}
