create or replace function public.preparar_receta(p_descuentos jsonb)
returns void
language plpgsql
security invoker
set search_path = public
as $$
declare
    descuento jsonb;
    ingrediente public.ingredientes%rowtype;
    ingrediente_id bigint;
    cantidad_descuento real;
begin
    if p_descuentos is null or jsonb_typeof(p_descuentos) <> 'array' then
        raise exception 'La lista de descuentos debe ser un arreglo';
    end if;

    for descuento in select value from jsonb_array_elements(p_descuentos)
    loop
        ingrediente_id := (descuento ->> 'ingrediente_id')::bigint;
        cantidad_descuento := (descuento ->> 'cantidad')::real;

        if cantidad_descuento is null or cantidad_descuento <= 0 then
            raise exception 'Cantidad de descuento inválida';
        end if;

        select *
        into ingrediente
        from public.ingredientes
        where id = ingrediente_id
          and user_id = auth.uid()
        for update;

        if not found then
            raise exception 'Ingrediente no encontrado';
        end if;

        if ingrediente.cantidad < cantidad_descuento then
            raise exception 'Inventario insuficiente para %', ingrediente.nombre;
        end if;

        update public.ingredientes
        set cantidad = cantidad - cantidad_descuento
        where id = ingrediente_id
          and user_id = auth.uid();

        insert into public.historial_consumo (
            nombre,
            cantidad,
            unidad,
            categoria_id,
            fecha_consumo,
            user_id
        ) values (
            ingrediente.nombre,
            cantidad_descuento,
            ingrediente.unidad,
            ingrediente.categoria_id,
            now(),
            auth.uid()
        );
    end loop;
end;
$$;

grant execute on function public.preparar_receta(jsonb) to authenticated;
