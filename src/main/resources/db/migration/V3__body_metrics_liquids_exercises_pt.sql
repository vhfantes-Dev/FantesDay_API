-- ---------- Usuario: altura, nascimento e nivel de atividade (TMB e gasto diario) ----------
alter table users add column height_cm numeric(5,1);
alter table users add column birth_date date;
alter table users add column activity_level varchar(255);
alter table users add constraint users_activity_level_check
    check (activity_level in ('SEDENTARY','LIGHT','MODERATE','ACTIVE','VERY_ACTIVE'));

-- ---------- Alimentos: liquidos registrados em ml ----------
alter table foods add column liquid boolean not null default false;
alter table foods add column density_g_per_ml numeric(5,3);

-- Liquidos da TACO (sucos, oleos, leites, bebidas, mel...) com a densidade em g/ml.
-- Em um banco novo a TACO ainda nao foi importada: o CatalogSeeder ja grava esses valores (seed/taco.json).
update foods f
set liquid = true, density_g_per_ml = v.density, default_unit = 'ml'
from (values
    ('188', 1.05), ('209', 1.04), ('211', 1.04), ('213', 1.04), ('215', 1.04), ('217', 1.04), ('218', 1.03),
    ('219', 1.03), ('234', 1.05), ('252', 1.04), ('258', 1.06), ('259', 0.91), ('260', 0.91), ('267', 0.92),
    ('268', 0.92), ('269', 0.92), ('270', 0.92), ('271', 0.92), ('272', 0.92), ('446', 1.05), ('454', 1.03),
    ('455', 1.06), ('457', 1.03), ('458', 1.03), ('460', 1.05), ('470', 1.02), ('471', 1.00), ('472', 0.95),
    ('473', 1.07), ('474', 1.01), ('475', 1.00), ('476', 1.00), ('477', 1.00), ('478', 1.02), ('479', 1.03),
    ('480', 1.04), ('481', 1.04), ('482', 1.04), ('483', 1.04), ('507', 1.42), ('508', 1.35), ('523', 1.00)
) as v(external_id, density)
where f.source = 'TACO' and f.external_id = v.external_id;

-- ---------- Exercicios: nome em portugues + nome original, idioma das instrucoes e nova fonte (wger) ----------
alter table exercises add column name_en varchar(255);
alter table exercises add column instructions_language varchar(2);

alter table exercises drop constraint if exists exercises_source_check;
alter table exercises add constraint exercises_source_check
    check (source in ('TACO','OPEN_FOOD_FACTS','FREE_EXERCISE_DB','WGER','CUSTOM'));

alter table foods drop constraint if exists foods_source_check;
alter table foods add constraint foods_source_check
    check (source in ('TACO','OPEN_FOOD_FACTS','FREE_EXERCISE_DB','WGER','CUSTOM'));

create index if not exists idx_exercises_muscle_group on exercises (lower(muscle_group));
