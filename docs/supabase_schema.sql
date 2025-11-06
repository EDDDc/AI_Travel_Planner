-- 基础表结构（最小化）与 RLS 策略

-- 需要生成 UUID 的函数（Supabase 通常已启用，如未启用则创建）
create extension if not exists pgcrypto;

-- 用户档案（用于存储个人资料与偏好），与 auth.users 对齐
create table if not exists public.profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  avatar_url text,
  preferences jsonb default '{}',
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

create index if not exists idx_profiles_user on public.profiles(user_id);

alter table public.profiles enable row level security;

drop policy if exists "profiles_owner_select" on public.profiles;
create policy "profiles_owner_select" on public.profiles
  for select using (auth.uid() = user_id);

drop policy if exists "profiles_owner_mod" on public.profiles;
create policy "profiles_owner_mod" on public.profiles
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

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

-- 注意：PostgreSQL 不支持 CREATE POLICY IF NOT EXISTS
-- 先尝试删除旧策略，再创建新策略
drop policy if exists "itineraries_owner_select" on public.itineraries;
create policy "itineraries_owner_select" on public.itineraries
  for select using (auth.uid() = user_id);

drop policy if exists "itineraries_owner_mod" on public.itineraries;
create policy "itineraries_owner_mod" on public.itineraries
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

drop policy if exists "activities_owner_select" on public.activities;
create policy "activities_owner_select" on public.activities
  for select using (exists (
    select 1 from public.itineraries i where i.id = activities.itinerary_id and i.user_id = auth.uid()
  ));

drop policy if exists "activities_owner_mod" on public.activities;
create policy "activities_owner_mod" on public.activities
  for all using (exists (
    select 1 from public.itineraries i where i.id = activities.itinerary_id and i.user_id = auth.uid()
  )) with check (exists (
    select 1 from public.itineraries i where i.id = activities.itinerary_id and i.user_id = auth.uid()
  ));

drop policy if exists "budget_owner_select" on public.budget_entries;
create policy "budget_owner_select" on public.budget_entries
  for select using (exists (
    select 1 from public.itineraries i where i.id = budget_entries.itinerary_id and i.user_id = auth.uid()
  ));

drop policy if exists "budget_owner_mod" on public.budget_entries;
create policy "budget_owner_mod" on public.budget_entries
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

drop trigger if exists set_profiles_updated on public.profiles;
create trigger set_profiles_updated
before update on public.profiles
for each row execute procedure public.set_updated_at();
