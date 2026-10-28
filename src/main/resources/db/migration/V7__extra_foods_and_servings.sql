-- Base complementar de alimentos (pizzas, fast food, lanches, doces, suplementos, bebidas), com fonte FANTESDAY,
-- e porcao tipica de cada alimento (ex: 1 Big Mac = 211 g, 1 fatia de pizza = 110 g).
alter table foods add column serving_grams numeric(7, 1);
alter table foods add column serving_description varchar(100);

alter table foods drop constraint if exists foods_source_check;
alter table foods add constraint foods_source_check
    check (source in ('TACO','OPEN_FOOD_FACTS','FREE_EXERCISE_DB','WGER','CUSTOM','FANTESDAY'));
