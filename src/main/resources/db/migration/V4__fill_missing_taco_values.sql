-- A TACO (4a edicao) nao traz valores para alguns alimentos ("*" = nao analisado; "NA" = nao se aplica).
-- Leites: valores da USDA FoodData Central (dominio publico) por 100 g. Sal: sem calorias nem macronutrientes.
-- Em um banco novo a TACO ainda nao foi importada aqui; o CatalogSeeder ja grava esses valores (seed/taco.json).
update foods f
set calories_per_100g = v.kcal, protein_per_100g = v.protein, carbs_per_100g = v.carbs,
    fat_per_100g = v.fat, fiber_per_100g = v.fiber
from (values
    ('458', 61.00, 3.15, 4.80, 3.25, 0.00),  -- Leite, de vaca, integral (USDA FDC 171265)
    ('457', 35.00, 3.40, 4.85, 0.18, 0.00),  -- Leite, de vaca, desnatado, UHT (USDA FDC 169868)
    ('516', 0.00, 0.00, 0.00, 0.00, 0.00),   -- Sal, dietetico
    ('517', 0.00, 0.00, 0.00, 0.00, 0.00)    -- Sal, grosso
) as v(external_id, kcal, protein, carbs, fat, fiber)
where f.source = 'TACO' and f.external_id = v.external_id and f.calories_per_100g is null;
