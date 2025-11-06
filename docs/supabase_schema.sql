-- 基础表结构（最小化）与 RLS 策略

create table if not exists public.itineraries (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null,
  title text,
  destination text,
  start_date date,
  end_date date,
  people int,
  budget_target int,
  preferences_json jsonb default '{}',
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

create table if not exists public.activities (
  id uuid primary key default gen_random_uuid(),
  itinerary_id uuid references public.itineraries(id) on delete cascade,
  date date,
  type text,
  name text,
  lat double precision,
  lng double precision,
  start time,
  "end" time,
  cost_estimate int,
  extra_json jsonb default '{}'
);

create table if not exists public.budget_entries (
  id uuid primary key default gen_random_uuid(),
  itinerary_id uuid references public.itineraries(id) on delete cascade,
  date date,
  amount numeric,
  currency text default 'CNY',
  category text,
  note text,
  source text default 'manual',
  created_at timestamptz default now()
);

-- 索引
create index if not exists idx_itineraries_user on public.itineraries(user_id);
create index if not exists idx_activities_itinerary on public.activities(itinerary_id);
create index if not exists idx_budget_itinerary on public.budget_entries(itinerary_id);

-- 启用 RLS
alter table public.itineraries enable row level security;
alter table public.activities enable row level security;
alter table public.budget_entries enable row level security;

-- RLS 策略：仅允许本人读写自己的数据
create policy if not exists "itineraries_owner_select" on public.itineraries
  for select using (auth.uid() = user_id);
create policy if not exists "itineraries_owner_mod" on public.itineraries
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy if not exists "activities_owner_select" on public.activities
  for select using (exists (
    select 1 from public.itineraries i where i.id = activities.itinerary_id and i.user_id = auth.uid()
  ));
create policy if not exists "activities_owner_mod" on public.activities
  for all using (exists (
    select 1 from public.itineraries i where i.id = activities.itinerary_id and i.user_id = auth.uid()
  )) with check (exists (
    select 1 from public.itineraries i where i.id = activities.itinerary_id and i.user_id = auth.uid()
  ));

create policy if not exists "budget_owner_select" on public.budget_entries
  for select using (exists (
    select 1 from public.itineraries i where i.id = budget_entries.itinerary_id and i.user_id = auth.uid()
  ));
create policy if not exists "budget_owner_mod" on public.budget_entries
  for all using (exists (
    select 1 from public.itineraries i where i.id = budget_entries.itinerary_id and i.user_id = auth.uid()
  )) with check (exists (
    select 1 from public.itineraries i where i.id = budget_entries.itinerary_id and i.user_id = auth.uid()
  ));

-- 触发器：更新 updated_at
create or replace function public.set_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

drop trigger if exists set_itineraries_updated on public.itineraries;
create trigger set_itineraries_updated
before update on public.itineraries
for each row execute procedure public.set_updated_at();

