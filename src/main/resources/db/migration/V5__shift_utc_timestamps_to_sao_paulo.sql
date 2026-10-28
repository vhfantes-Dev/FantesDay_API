-- Ate aqui a API rodava em UTC: as colunas timestamp (sem fuso) e o "hoje" do servidor ficavam 3h adiantados
-- em relacao a America/Sao_Paulo (sem horario de verao desde 2019). Agora a JVM usa o fuso de Sao Paulo
-- (FantesdayApplication), entao os registros antigos sao trazidos para o mesmo fuso.

-- ---------- Datas preenchidas pelo servidor entre 21h e 0h (ja eram o dia seguinte em UTC) ----------
-- So mexe quando a data bate com o dia em UTC e o horario local cai no dia anterior: nesse caso ela veio
-- do LocalDate.now() do servidor, e nao do app.
update task_completions tc
set date = cast(tc.completed_at - interval '3 hours' as date)
where tc.date = cast(tc.completed_at as date)
  and cast(tc.completed_at - interval '3 hours' as date) <> tc.date
  and not exists (select 1 from task_completions other
                  where other.task_id = tc.task_id
                    and other.date = cast(tc.completed_at - interval '3 hours' as date));

update water_entries
set date = cast(created_at - interval '3 hours' as date)
where date = cast(created_at as date)
  and cast(created_at - interval '3 hours' as date) <> date;

update meals
set date = cast(created_at - interval '3 hours' as date)
where date = cast(created_at as date)
  and cast(created_at - interval '3 hours' as date) <> date;

-- ---------- Horarios gravados em UTC -> Sao Paulo ----------
update users set created_at = created_at - interval '3 hours',
                 updated_at = updated_at - interval '3 hours',
                 photo_updated_at = photo_updated_at - interval '3 hours';
update progress set updated_at = updated_at - interval '3 hours';
update nutrition_goals set updated_at = updated_at - interval '3 hours';
update meals set created_at = created_at - interval '3 hours';
update workout_sets set created_at = created_at - interval '3 hours';
update task_completions set completed_at = completed_at - interval '3 hours';
update workout_routines set created_at = created_at - interval '3 hours',
                            updated_at = updated_at - interval '3 hours';
update water_entries set created_at = created_at - interval '3 hours';
update weight_entries set updated_at = updated_at - interval '3 hours';
update user_photos set updated_at = updated_at - interval '3 hours';
