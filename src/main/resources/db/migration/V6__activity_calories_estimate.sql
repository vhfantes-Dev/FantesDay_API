-- Calorias das atividades estimadas pelo MET (kcal = MET x peso x horas) quando o app nao informa o gasto.
-- met: MET usado na estimativa; calories_estimated: true quando as calorias foram calculadas pela API
-- (e entao sao recalculadas se a duracao ou o tipo mudar).
alter table activities add column met numeric(4, 1);
alter table activities add column calories_estimated boolean not null default false;
