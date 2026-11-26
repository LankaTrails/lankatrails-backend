-- LankaTrails demo data for local development.
--
-- Derived from the July 2025 interim dump (data only) and mapped onto the current schema.
-- Contact details are fictional; two listings that used real hotel brands were renamed.
-- Load with docker/seed/load-seed.sh, which supplies the demo password (DEMO_USER_PASSWORD).
-- Re-running is safe: nothing happens if the demo provider already exists.
-- Requires the backend to have started once (Hibernate creates the schema and lookup rows).

\set ON_ERROR_STOP on
BEGIN;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
SELECT set_config('lankatrails.demo_password', :'demo_password', false);

DO $seed$
DECLARE
  pw text := crypt(current_setting('lankatrails.demo_password'), gen_salt('bf', 10));
  v_provider bigint; v_tourist bigint; v_tourist2 bigint; v_contact bigint; v_loc bigint;
  v_price bigint; v_booking_cfg bigint; v_svc bigint; v_policy bigint; v_trip bigint;
  v_part bigint; v_part2 bigint; v_item bigint; v_booking bigint; v_room bigint; v_complaint bigint;
  s jsonb := '{}'::jsonb;   -- original service id -> new service id
BEGIN
  IF EXISTS (SELECT 1 FROM users WHERE email = 'olanka@lankatrails.dev') THEN
    RAISE NOTICE 'LankaTrails demo data already present; nothing to do.';
    RETURN;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM categories) THEN
    RAISE EXCEPTION 'Lookup tables are empty. Start the backend once so it creates them, then re-run.';
  END IF;


  -- Provider: Olanka Travels
  INSERT INTO users (email, email_verified, password, profile_picture_url, role, status, created_at, updated_at)
  VALUES ('olanka@lankatrails.dev', true, pw, '/uploads/profile-pictures/demo-olanka-logo.svg',
    'ROLE_PROVIDER', 'ACTIVE', now() - interval '120 days', now())
  RETURNING user_id INTO v_provider;
  INSERT INTO user_preferences (user_id, preferred_currency, time_zone, language, is_24_hour, measurement_system,
    notifications_enabled, email_notifications, push_notifications, sms_notifications)
  VALUES (v_provider, 'LKR', 'Asia/Colombo', 'en-LK', true, 'metric', true, true, true, false);
  INSERT INTO contact_persons (name, email, phone_number, position, identity_document_url)
  VALUES ('Kasun Perera', 'kasun.perera@lankatrails.dev', '0770000001', 'Manager',
    '/uploads/identification/demo-contact-id.svg')
  RETURNING contact_person_id INTO v_contact;
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Matara', 'Sri Lanka', 'Matara', 'Southern Province', 'WGXW+VP5, Matara, Sri Lanka',
    NULL, 5.9496309, 80.54685289999999, ST_SetSRID(ST_MakePoint(80.54685289999999, 5.9496309), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO providers (user_id, business_name, business_description, business_type, br_number, br_url, cover_image_url,
    accommodation_approval_status, transport_approval_status, food_approval_status, activity_approval_status, tour_guide_approval_status,
    contact_person_id, location_id)
  VALUES (v_provider, 'Olanka Travels', 'Olanka Travels is a premier destination management company based in Sri Lanka, specializing in customized travel experiences for both local and international tourists.', 'COMPANY', 'BR/COM/123',
    '/uploads/business-registration/demo-olanka-br.svg',
    '/uploads/cover-pictures/demo-olanka-cover.svg',
    'APPROVED', 'APPROVED', 'APPROVED', 'PENDING', 'NOT_REQUESTED', v_contact, v_loc);
  INSERT INTO licenses (license_number, expiry_date, license_url, category_id, provider_id)
  VALUES ('LN/12/1234', current_date + interval '1 year', '/uploads/licence/demo-accommodation-licence.svg', 1, v_provider);
  INSERT INTO licenses (license_number, expiry_date, license_url, category_id, provider_id)
  VALUES ('TR/45/2026', current_date + interval '1 year', '/uploads/licence/demo-transport-licence.svg', 2, v_provider);
  INSERT INTO licenses (license_number, expiry_date, license_url, category_id, provider_id)
  VALUES ('FB/88/2026', current_date + interval '1 year', '/uploads/licence/demo-food-licence.svg', 3, v_provider);
  INSERT INTO licenses (license_number, expiry_date, license_url, category_id, provider_id)
  VALUES ('AC/17/2026', current_date + interval '1 year', '/uploads/licence/demo-activity-licence.svg', 4, v_provider);

  -- Tourists
  INSERT INTO users (email, email_verified, password, role, status, created_at, updated_at)
  VALUES ('tourist@lankatrails.dev', true, pw, 'ROLE_TOURIST', 'ACTIVE', now() - interval '90 days', now())
  RETURNING user_id INTO v_tourist;
  INSERT INTO tourists (user_id, first_name, last_name, phone_number, country) VALUES (v_tourist, 'Amaya', 'Silva', '0770000002', 'LK');
  INSERT INTO user_preferences (user_id, preferred_currency, time_zone, language, is_24_hour, measurement_system,
    notifications_enabled, email_notifications, push_notifications, sms_notifications)
  VALUES (v_tourist, 'LKR', 'Asia/Colombo', 'en-LK', true, 'metric', true, true, true, false);
  INSERT INTO users (email, email_verified, password, role, status, created_at, updated_at)
  VALUES ('tourist2@lankatrails.dev', true, pw, 'ROLE_TOURIST', 'ACTIVE', now() - interval '90 days', now())
  RETURNING user_id INTO v_tourist2;
  INSERT INTO tourists (user_id, first_name, last_name, phone_number, country) VALUES (v_tourist2, 'Ravi', 'Fernando', '0770000003', 'LK');
  INSERT INTO user_preferences (user_id, preferred_currency, time_zone, language, is_24_hour, measurement_system,
    notifications_enabled, email_notifications, push_notifications, sms_notifications)
  VALUES (v_tourist2, 'LKR', 'Asia/Colombo', 'en-LK', true, 'metric', true, true, true, false);

  -- Services (18) with pricing, booking rules, opening hours, location, images and tabs

  -- Whale Watching (ACTIVITY)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_PERSON', NULL, NULL, 15000, 7500, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, slot_duration, buffer_time, manage_capacity, require_child_info, allow_extra_capacity,
    allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('TIME_SLOTS', 10, 4, 2, 1, 4, 180, 30, true, false, false, true, 60, 2) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Whale Watching', '0770000001', 'ACTIVE', 0, 4, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('1', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Matara', 'Sri Lanka', 'Matara', 'Southern Province', 'WGXW+VP5, Matara, Sri Lanka',
    NULL, 5.9496309, 80.54685289999999, ST_SetSRID(ST_MakePoint(80.54685289999999, 5.9496309), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO activity_services (service_id, activity_category_id, activity_details, safety_instructions)
  VALUES (v_svc, 4, 'Sri Lanka is one of the top whale watching destinations in the world, offering a rare opportunity to witness majestic marine giants such as blue whales, sperm whales, Bryde’s whales, and various dolphin species close to the shore.', 'Maintain a safe distance from whales
Avoid disturbing marine life
Support eco-conscious tour operators');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '07:00', '18:00', false, false),
    (v_svc, 'TUESDAY', '07:00', '18:00', false, false),
    (v_svc, 'WEDNESDAY', '07:00', '18:00', false, false),
    (v_svc, 'THURSDAY', '07:00', '18:00', false, false),
    (v_svc, 'FRIDAY', '07:00', '18:00', false, false),
    (v_svc, 'SATURDAY', '07:00', '18:00', false, false),
    (v_svc, 'SUNDAY', '07:00', '18:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-whale-watching-1.svg');

  -- Olanka CityBus (TRANSPORT)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_KM', NULL, 200, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings,
    advance_booking_period, last_minute_booking_period)
  VALUES ('WHOLE_DAY', 3, 20, 0, 1, 3, true, false, false, true, 90, 12) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Olanka CityBus', '0770000002', 'ACTIVE', 0, 2, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('102', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Galle', 'Sri Lanka', 'Galle', 'Southern Province', 'Wakwella Rd, Galle 80000, Sri Lanka',
    '80000', 6.032894799999999, 80.2167912, ST_SetSRID(ST_MakePoint(80.2167912, 6.032894799999999), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO transport (service_id, vehicle_category_id, air_conditioned, driver_included, fuel_type, transmission_type)
  VALUES (v_svc, 3, true, true,
    'PETROL', 'SEMI_AUTOMATIC');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '06:00', '22:00', false, false),
    (v_svc, 'TUESDAY', '06:00', '22:00', false, false),
    (v_svc, 'WEDNESDAY', '06:00', '22:00', false, false),
    (v_svc, 'THURSDAY', '06:00', '22:00', false, false),
    (v_svc, 'FRIDAY', '06:00', '22:00', false, false),
    (v_svc, 'SATURDAY', '06:00', '22:00', false, false),
    (v_svc, 'SUNDAY', '06:00', '22:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-olanka-citybus-1.svg');

  -- Tasty Cabin (FOOD_BEVERAGE)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_PERSON', NULL, NULL, 2500, 1250, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, slot_duration, buffer_time, manage_capacity, require_child_info, allow_extra_capacity,
    allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('TIME_SLOTS', 15, 4, 2, 1, 4, 90, 30, true, false, false, true, 60, 2) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Tasty Cabin', '0770000003', 'ACTIVE', 0, 3, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('103', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Weligama', 'Sri Lanka', 'Matara', 'Southern Province', 'Mudugamuwa Adahanagaraya, Weligama, Sri Lanka',
    NULL, 5.984687927409956, 80.42401118017732, ST_SetSRID(ST_MakePoint(80.42401118017732, 5.984687927409956), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO food_and_beverage (service_id, food_and_beverage_category_id, cuisine_type, alcohol_served, halal_certified,
    live_music, outdoor_seating, vegetarian_options)
  VALUES (v_svc, 1, 'Italian', true, false,
    true, true, true);
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '08:00', '23:00', false, false),
    (v_svc, 'TUESDAY', '08:00', '23:00', false, false),
    (v_svc, 'WEDNESDAY', '08:00', '23:00', false, false),
    (v_svc, 'THURSDAY', '08:00', '23:00', false, false),
    (v_svc, 'FRIDAY', '08:00', '23:00', false, false),
    (v_svc, 'SATURDAY', '08:00', '23:00', false, false),
    (v_svc, 'SUNDAY', '08:00', '23:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-tasty-cabin-1.svg');

  -- The Tree House (ACCOMMODATION)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('FIXED', 23000, NULL, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, default_check_in_time, default_check_out_time, minimum_booking_days, maximum_booking_days,
    manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('MULTI_DAY', 4, 4, 1, 1, 3, '14:00', '11:00', 1, 14, true, false, false, true, 180, 24) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'The Tree House', '0770000004', 'ACTIVE', 0, 1, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('104', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Ahangama', 'Sri Lanka', 'Galle', 'Southern Province', 'X9XC+H9 Ahangama, Sri Lanka',
    NULL, 5.998892140681078, 80.37094716504214, ST_SetSRID(ST_MakePoint(80.37094716504214, 5.998892140681078), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO accommodation (service_id, accommodation_category_id, air_conditioned, breakfast_included, free_wifi, gym_access,
    laundry_service, parking_available, pet_friendly, room_service, spa_services, swimming_pool)
  VALUES (v_svc, 1, true, true, true,
    false, false, true, true, true,
    false, false);
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '00:00', '23:59', true, false),
    (v_svc, 'TUESDAY', '00:00', '23:59', true, false),
    (v_svc, 'WEDNESDAY', '00:00', '23:59', true, false),
    (v_svc, 'THURSDAY', '00:00', '23:59', true, false),
    (v_svc, 'FRIDAY', '00:00', '23:59', true, false),
    (v_svc, 'SATURDAY', '00:00', '23:59', true, false),
    (v_svc, 'SUNDAY', '00:00', '23:59', true, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-the-tree-house-1.svg');
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-the-tree-house-2.svg');

  -- Fort Place (ACCOMMODATION)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('FIXED', 25000, NULL, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, default_check_in_time, default_check_out_time, minimum_booking_days, maximum_booking_days,
    manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('MULTI_DAY', 4, 4, 1, 1, 3, '14:00', '11:00', 1, 14, true, false, false, true, 180, 24) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Fort Place', '0770000005', 'ACTIVE', 0, 1, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('105', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Kamburugamuwa', 'Sri Lanka', 'Matara', 'Southern Province', 'XFFP+8HG, Kotawila Mahawella Rd, Kamburugamuwa, Sri Lanka',
    NULL, 5.972510515693494, 80.486741580942, ST_SetSRID(ST_MakePoint(80.486741580942, 5.972510515693494), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO accommodation (service_id, accommodation_category_id, air_conditioned, breakfast_included, free_wifi, gym_access,
    laundry_service, parking_available, pet_friendly, room_service, spa_services, swimming_pool)
  VALUES (v_svc, 2, true, true, true,
    false, false, true, true, true,
    false, false);
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '00:00', '23:59', true, false),
    (v_svc, 'TUESDAY', '00:00', '23:59', true, false),
    (v_svc, 'WEDNESDAY', '00:00', '23:59', true, false),
    (v_svc, 'THURSDAY', '00:00', '23:59', true, false),
    (v_svc, 'FRIDAY', '00:00', '23:59', true, false),
    (v_svc, 'SATURDAY', '00:00', '23:59', true, false),
    (v_svc, 'SUNDAY', '00:00', '23:59', true, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-fort-place-1.svg');
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-fort-place-2.svg');

  -- Climbers Adventure (ACTIVITY)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_PERSON', NULL, NULL, 3000, 1500, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, slot_duration, buffer_time, manage_capacity, require_child_info, allow_extra_capacity,
    allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('TIME_SLOTS', 10, 4, 2, 1, 4, 180, 30, true, false, false, true, 60, 2) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Climbers Adventure', '0770000007', 'ACTIVE', 0, 4, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('107', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Matara', 'Sri Lanka', 'Matara', 'Southern Province', 'XG54+J7P, Matara, Sri Lanka',
    NULL, 5.9581460088455, 80.50637138459543, ST_SetSRID(ST_MakePoint(80.50637138459543, 5.9581460088455), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO activity_services (service_id, activity_category_id, activity_details, safety_instructions)
  VALUES (v_svc, 1, 'Sri Lanka offers a variety of rock climbing and trekking adventures for thrill-seekers and nature lovers alike. From sacred mountains and misty peaks to granite cliffs and jungle-covered trails, ', 'Always follow instructions from tour guides, park rangers, or activity organizers.
Respect sacred sites and local customs—dress modestly at temples and remove shoes when required');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '07:00', '18:00', false, false),
    (v_svc, 'TUESDAY', '07:00', '18:00', false, false),
    (v_svc, 'WEDNESDAY', '07:00', '18:00', false, false),
    (v_svc, 'THURSDAY', '07:00', '18:00', false, false),
    (v_svc, 'FRIDAY', '07:00', '18:00', false, false),
    (v_svc, 'SATURDAY', '07:00', '18:00', false, false),
    (v_svc, 'SUNDAY', '07:00', '18:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-climbers-adventure-1.svg');

  -- Lighthouse Adventure (ACTIVITY)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_PERSON', NULL, NULL, 3000, 1500, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, slot_duration, buffer_time, manage_capacity, require_child_info, allow_extra_capacity,
    allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('TIME_SLOTS', 10, 4, 2, 1, 4, 180, 30, true, false, false, true, 60, 2) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Lighthouse Adventure', '0770000052', 'ACTIVE', 0, 4, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('152', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Unawatuna', 'Sri Lanka', 'Galle', 'Southern Province', '27C5+V2, Unawatuna, Sri Lanka',
    NULL, 6.021791982454172, 80.2572563817882, ST_SetSRID(ST_MakePoint(80.2572563817882, 6.021791982454172), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO activity_services (service_id, activity_category_id, activity_details, safety_instructions)
  VALUES (v_svc, 1, 'Step back in time and witness the maritime legacy of Sri Lanka with a Lighthouse Adventure — a unique coastal journey to discover the island’s historic beacons that once guided ancient seafarers and colonial ships.', 'Always follow your guide’s instructions and stay with the group.
Respect local customs, wildlife, and cultural sites.
Carry a copy of your ID or passport at all times.');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '07:00', '18:00', false, false),
    (v_svc, 'TUESDAY', '07:00', '18:00', false, false),
    (v_svc, 'WEDNESDAY', '07:00', '18:00', false, false),
    (v_svc, 'THURSDAY', '07:00', '18:00', false, false),
    (v_svc, 'FRIDAY', '07:00', '18:00', false, false),
    (v_svc, 'SATURDAY', '07:00', '18:00', false, false),
    (v_svc, 'SUNDAY', '07:00', '18:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-lighthouse-adventure-1.svg');

  -- Surfing Club (ACTIVITY)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_PERSON', NULL, NULL, 2000, 1000, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, slot_duration, buffer_time, manage_capacity, require_child_info, allow_extra_capacity,
    allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('TIME_SLOTS', 10, 4, 2, 1, 4, 180, 30, true, false, false, true, 60, 2) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Surfing Club', '0770000002', 'ACTIVE', 0, 4, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('202', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Matara', 'Sri Lanka', 'Matara', 'Southern Province', 'XC55+FPX, Sri Lanka',
    NULL, 5.95830301455009, 80.40932701774436, ST_SetSRID(ST_MakePoint(80.40932701774436, 5.95830301455009), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO activity_services (service_id, activity_category_id, activity_details, safety_instructions)
  VALUES (v_svc, 7, 'Surfing is an exhilarating water sport where individuals ride on the forward or deep face of a moving wave, typically in the ocean. Surfers use a surfboard to balance and glide across the water, harnessing the power of waves for momentum.', 'Check local surf reports
Know your limits
Watch out for riptides and currents
Be aware of marine life (jellyfish, sharks)
Follow surf etiquette (right of way, don’t drop in)

');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '07:00', '18:00', false, false),
    (v_svc, 'TUESDAY', '07:00', '18:00', false, false),
    (v_svc, 'WEDNESDAY', '07:00', '18:00', false, false),
    (v_svc, 'THURSDAY', '07:00', '18:00', false, false),
    (v_svc, 'FRIDAY', '07:00', '18:00', false, false),
    (v_svc, 'SATURDAY', '07:00', '18:00', false, false),
    (v_svc, 'SUNDAY', '07:00', '18:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-surfing-club-1.svg');

  -- CarQuotes (TRANSPORT)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_KM', NULL, 250, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings,
    advance_booking_period, last_minute_booking_period)
  VALUES ('WHOLE_DAY', 2, 4, 0, 1, 2, true, false, false, true, 90, 12) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'CarQuotes', '0770000003', 'ACTIVE', 0, 2, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('203', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Matara', 'Sri Lanka', 'Matara', 'Southern Province', 'WGXW+VP5, Matara, Sri Lanka',
    NULL, 5.9496309, 80.54685289999999, ST_SetSRID(ST_MakePoint(80.54685289999999, 5.9496309), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO transport (service_id, vehicle_category_id, air_conditioned, driver_included, fuel_type, transmission_type)
  VALUES (v_svc, 9, true, false,
    'PETROL', 'AUTOMATIC');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '06:00', '22:00', false, false),
    (v_svc, 'TUESDAY', '06:00', '22:00', false, false),
    (v_svc, 'WEDNESDAY', '06:00', '22:00', false, false),
    (v_svc, 'THURSDAY', '06:00', '22:00', false, false),
    (v_svc, 'FRIDAY', '06:00', '22:00', false, false),
    (v_svc, 'SATURDAY', '06:00', '22:00', false, false),
    (v_svc, 'SUNDAY', '06:00', '22:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-carquotes-1.svg');
  INSERT INTO tabs_section (service_id, heading, content) VALUES (v_svc, 'Available Colours', '<p>Black and White</p>');

  -- AutoCabs (TRANSPORT)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_KM', NULL, 300, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings,
    advance_booking_period, last_minute_booking_period)
  VALUES ('WHOLE_DAY', 3, 4, 0, 1, 3, true, false, false, true, 90, 12) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'AutoCabs', '0770000004', 'ACTIVE', 0, 2, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('204', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Matara', 'Sri Lanka', 'Matara', 'Southern Province', 'WGXW+VP5, Matara, Sri Lanka',
    NULL, 5.9496309, 80.54685289999999, ST_SetSRID(ST_MakePoint(80.54685289999999, 5.9496309), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO transport (service_id, vehicle_category_id, air_conditioned, driver_included, fuel_type, transmission_type)
  VALUES (v_svc, 1, false, false,
    'PETROL', 'AUTOMATIC');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '06:00', '22:00', false, false),
    (v_svc, 'TUESDAY', '06:00', '22:00', false, false),
    (v_svc, 'WEDNESDAY', '06:00', '22:00', false, false),
    (v_svc, 'THURSDAY', '06:00', '22:00', false, false),
    (v_svc, 'FRIDAY', '06:00', '22:00', false, false),
    (v_svc, 'SATURDAY', '06:00', '22:00', false, false),
    (v_svc, 'SUNDAY', '06:00', '22:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-autocabs-1.svg');

  -- SuperBus Travels (TRANSPORT)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_KM', NULL, 400, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings,
    advance_booking_period, last_minute_booking_period)
  VALUES ('WHOLE_DAY', 2, 30, 0, 1, 2, true, false, false, true, 90, 12) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'SuperBus Travels', '0770000005', 'ACTIVE', 0, 2, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('205', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Galle', 'Sri Lanka', 'Galle', 'Southern Province', '26MC+MR Galle, Sri Lanka',
    NULL, 6.034212653283967, 80.22200412127245, ST_SetSRID(ST_MakePoint(80.22200412127245, 6.034212653283967), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO transport (service_id, vehicle_category_id, air_conditioned, driver_included, fuel_type, transmission_type)
  VALUES (v_svc, 3, true, true,
    'DIESEL', 'MANUAL');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '06:00', '22:00', false, false),
    (v_svc, 'TUESDAY', '06:00', '22:00', false, false),
    (v_svc, 'WEDNESDAY', '06:00', '22:00', false, false),
    (v_svc, 'THURSDAY', '06:00', '22:00', false, false),
    (v_svc, 'FRIDAY', '06:00', '22:00', false, false),
    (v_svc, 'SATURDAY', '06:00', '22:00', false, false),
    (v_svc, 'SUNDAY', '06:00', '22:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-superbus-travels-1.svg');

  -- Matara Dragons (FOOD_BEVERAGE)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_PERSON', NULL, NULL, 3000, 1500, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, slot_duration, buffer_time, manage_capacity, require_child_info, allow_extra_capacity,
    allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('TIME_SLOTS', 15, 4, 2, 1, 4, 90, 30, true, false, false, true, 60, 2) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Matara Dragons', '0770000006', 'ACTIVE', 0, 3, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('206', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Mirissa', 'Sri Lanka', 'Matara', 'Southern Province', 'XF96+VV5, Mirissa, Sri Lanka',
    NULL, 5.969888653759644, 80.46197898447754, ST_SetSRID(ST_MakePoint(80.46197898447754, 5.969888653759644), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO food_and_beverage (service_id, food_and_beverage_category_id, cuisine_type, alcohol_served, halal_certified,
    live_music, outdoor_seating, vegetarian_options)
  VALUES (v_svc, 1, 'Sri Lankan, Chineese', true, false,
    true, true, true);
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '08:00', '23:00', false, false),
    (v_svc, 'TUESDAY', '08:00', '23:00', false, false),
    (v_svc, 'WEDNESDAY', '08:00', '23:00', false, false),
    (v_svc, 'THURSDAY', '08:00', '23:00', false, false),
    (v_svc, 'FRIDAY', '08:00', '23:00', false, false),
    (v_svc, 'SATURDAY', '08:00', '23:00', false, false),
    (v_svc, 'SUNDAY', '08:00', '23:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-matara-dragons-1.svg');

  -- Olu Restaurant (FOOD_BEVERAGE)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('FIXED', 1500, NULL, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, slot_duration, buffer_time, manage_capacity, require_child_info, allow_extra_capacity,
    allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('TIME_SLOTS', 15, 4, 2, 1, 4, 90, 30, true, false, false, true, 60, 2) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Olu Restaurant', '0770000007', 'ACTIVE', 0, 3, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('207', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Mirissa', 'Sri Lanka', 'Matara', 'Southern Province', 'WFQ9+WMF, A2, Mirissa, Sri Lanka',
    NULL, 5.940004594437964, 80.46915747807361, ST_SetSRID(ST_MakePoint(80.46915747807361, 5.940004594437964), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO food_and_beverage (service_id, food_and_beverage_category_id, cuisine_type, alcohol_served, halal_certified,
    live_music, outdoor_seating, vegetarian_options)
  VALUES (v_svc, 1, 'Sri Lankan, Japaneese, Chineese', true, true,
    true, true, true);
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '08:00', '23:00', false, false),
    (v_svc, 'TUESDAY', '08:00', '23:00', false, false),
    (v_svc, 'WEDNESDAY', '08:00', '23:00', false, false),
    (v_svc, 'THURSDAY', '08:00', '23:00', false, false),
    (v_svc, 'FRIDAY', '08:00', '23:00', false, false),
    (v_svc, 'SATURDAY', '08:00', '23:00', false, false),
    (v_svc, 'SUNDAY', '08:00', '23:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-olu-restaurant-1.svg');

  -- Kamilia Resort (ACCOMMODATION)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_NIGHT', NULL, 10000, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, default_check_in_time, default_check_out_time, minimum_booking_days, maximum_booking_days,
    manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('MULTI_DAY', 5, 4, 1, 1, 3, '14:00', '11:00', 1, 14, true, false, false, true, 180, 24) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Kamilia Resort', '0770000008', 'ACTIVE', 0, 1, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('208', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Matara', 'Sri Lanka', 'Matara', 'Southern Province', 'XC55+FPX, Sri Lanka',
    NULL, 5.95846771054246, 80.40993881194225, ST_SetSRID(ST_MakePoint(80.40993881194225, 5.95846771054246), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO accommodation (service_id, accommodation_category_id, air_conditioned, breakfast_included, free_wifi, gym_access,
    laundry_service, parking_available, pet_friendly, room_service, spa_services, swimming_pool)
  VALUES (v_svc, 3, true, true, true,
    false, true, true, true, true,
    false, true);
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '00:00', '23:59', true, false),
    (v_svc, 'TUESDAY', '00:00', '23:59', true, false),
    (v_svc, 'WEDNESDAY', '00:00', '23:59', true, false),
    (v_svc, 'THURSDAY', '00:00', '23:59', true, false),
    (v_svc, 'FRIDAY', '00:00', '23:59', true, false),
    (v_svc, 'SATURDAY', '00:00', '23:59', true, false),
    (v_svc, 'SUNDAY', '00:00', '23:59', true, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-kamilia-resort-1.svg');
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-kamilia-resort-2.svg');

  -- Riverside Eco Lodge (ACCOMMODATION)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_NIGHT', NULL, 15000, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, default_check_in_time, default_check_out_time, minimum_booking_days, maximum_booking_days,
    manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('MULTI_DAY', 4, 1, 1, 1, 3, '14:00', '11:00', 1, 14, true, false, false, true, 180, 24) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Riverside Eco Lodge', '0770000053', 'ACTIVE', 0, 1, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('253', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Galle', 'Sri Lanka', 'Galle', 'Southern Province', '25XW+4C, Galle, Sri Lanka',
    NULL, 6.048060899999999, 80.1959016, ST_SetSRID(ST_MakePoint(80.1959016, 6.048060899999999), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO accommodation (service_id, accommodation_category_id, air_conditioned, breakfast_included, free_wifi, gym_access,
    laundry_service, parking_available, pet_friendly, room_service, spa_services, swimming_pool)
  VALUES (v_svc, 1, true, true, true,
    false, false, true, false, false,
    false, true);
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '00:00', '23:59', true, false),
    (v_svc, 'TUESDAY', '00:00', '23:59', true, false),
    (v_svc, 'WEDNESDAY', '00:00', '23:59', true, false),
    (v_svc, 'THURSDAY', '00:00', '23:59', true, false),
    (v_svc, 'FRIDAY', '00:00', '23:59', true, false),
    (v_svc, 'SATURDAY', '00:00', '23:59', true, false),
    (v_svc, 'SUNDAY', '00:00', '23:59', true, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-riverside-eco-lodge-1.svg');
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-riverside-eco-lodge-2.svg');
  INSERT INTO tabs_section (service_id, heading, content) VALUES (v_svc, 'About Us', '<p>Take a boat ride along a quiet rivulet that leads up to the lodge Kurulubedda , a scenic refuge in the coastal city of Galle that lies tucked away amidst lush greenery.</p>');
  INSERT INTO tabs_section (service_id, heading, content) VALUES (v_svc, 'About Meals', '<p>All meals at Riverside Eco Lodge are inspired by the verdant surroundings, with homegrown and freshly prepared produce served to you as exquisite culinary creations</p>');

  -- Weligama Bay Villas (ACCOMMODATION)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_NIGHT', NULL, 25000, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, default_check_in_time, default_check_out_time, minimum_booking_days, maximum_booking_days,
    manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings, advance_booking_period, last_minute_booking_period)
  VALUES ('MULTI_DAY', 3, 1, 1, 1, 3, '14:00', '11:00', 1, 14, true, false, false, true, 180, 24) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Weligama Bay Villas', '0770000006', 'ACTIVE', 0, 1, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('306', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Kanaththagoda', 'Sri Lanka', 'Galle', 'Southern Province', 'XG46+X3X, Kanaththagoda, Sri Lanka',
    NULL, 5.958190641795775, 80.50916782831023, ST_SetSRID(ST_MakePoint(80.50916782831023, 5.958190641795775), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO accommodation (service_id, accommodation_category_id, air_conditioned, breakfast_included, free_wifi, gym_access,
    laundry_service, parking_available, pet_friendly, room_service, spa_services, swimming_pool)
  VALUES (v_svc, 4, true, false, true,
    false, true, false, false, true,
    false, true);
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '00:00', '23:59', true, false),
    (v_svc, 'TUESDAY', '00:00', '23:59', true, false),
    (v_svc, 'WEDNESDAY', '00:00', '23:59', true, false),
    (v_svc, 'THURSDAY', '00:00', '23:59', true, false),
    (v_svc, 'FRIDAY', '00:00', '23:59', true, false),
    (v_svc, 'SATURDAY', '00:00', '23:59', true, false),
    (v_svc, 'SUNDAY', '00:00', '23:59', true, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-weligama-bay-villas-1.svg');
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-weligama-bay-villas-2.svg');
  INSERT INTO tabs_section (service_id, heading, content) VALUES (v_svc, 'About Us', '<p>This property is 2 minutes walk from the beach. Featuring barbecue facilities, a garden as well as a terrace, Weligama Bay Villas is situated in Weligama, 3.2 km from Kushtarajagala.</p>');
  INSERT INTO tabs_section (service_id, heading, content) VALUES (v_svc, 'About Rooms', '<p>All rooms feature a kitchen and a private bathroom. Free WiFi is offered. The units are fitted with a flat-screen TV with satellite channels, a kettle, a bath, a hair dryer and a desk.</p>');

  -- Matara Rentals (TRANSPORT)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_KM', NULL, 500, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings,
    advance_booking_period, last_minute_booking_period)
  VALUES ('WHOLE_DAY', 3, 4, 0, 1, 3, true, false, false, true, 90, 12) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Matara Rentals', '0770000007', 'ACTIVE', 0, 2, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('307', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Matara', 'Sri Lanka', 'Matara', 'Southern Province', 'XG54+J7P, Matara, Sri Lanka',
    NULL, 5.9581460088455, 80.50637138459543, ST_SetSRID(ST_MakePoint(80.50637138459543, 5.9581460088455), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO transport (service_id, vehicle_category_id, air_conditioned, driver_included, fuel_type, transmission_type)
  VALUES (v_svc, 1, true, false,
    'PETROL', 'AUTOMATIC');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '06:00', '22:00', false, false),
    (v_svc, 'TUESDAY', '06:00', '22:00', false, false),
    (v_svc, 'WEDNESDAY', '06:00', '22:00', false, false),
    (v_svc, 'THURSDAY', '06:00', '22:00', false, false),
    (v_svc, 'FRIDAY', '06:00', '22:00', false, false),
    (v_svc, 'SATURDAY', '06:00', '22:00', false, false),
    (v_svc, 'SUNDAY', '06:00', '22:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-matara-rentals-1.svg');

  -- Galle Tours (TRANSPORT)
  INSERT INTO price_configurations (price_type, fixed_price, price_per_unit, price_per_adult, price_per_child,
    requires_deposit, allow_advance_payment)
  VALUES ('PER_KM', NULL, 500, NULL, NULL, false, false) RETURNING price_config_id INTO v_price;
  INSERT INTO booking_configurations (booking_type, total_units, unit_adult_capacity, unit_child_capacity, min_units_per_booking,
    max_units_per_booking, manage_capacity, require_child_info, allow_extra_capacity, allow_back_to_back_bookings,
    advance_booking_period, last_minute_booking_period)
  VALUES ('WHOLE_DAY', 1, 20, 0, 1, 1, true, false, false, true, 90, 12) RETURNING booking_config_id INTO v_booking_cfg;
  -- services and complaint ids come from Hibernate's pooled sequences (increment 50); nextval values never collide with its blocks
  INSERT INTO services (service_id, service_name, contact_no, status, warnings, category_id, provider_id, price_config_id, booking_config_id)
  VALUES (nextval('services_seq'), 'Galle Tours', '0770000053', 'ACTIVE', 0, 2, v_provider, v_price, v_booking_cfg)
  RETURNING service_id INTO v_svc;
  s := s || jsonb_build_object('353', v_svc);
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Galle', 'Sri Lanka', 'Galle', 'Southern Province', '9 Bope Cross Rd, Galle 80000, Sri Lanka',
    '80000', 6.039348494103176, 80.2044797168997, ST_SetSRID(ST_MakePoint(80.2044797168997, 6.039348494103176), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO service_location (service_id, location_id) VALUES (v_svc, v_loc);
  INSERT INTO transport (service_id, vehicle_category_id, air_conditioned, driver_included, fuel_type, transmission_type)
  VALUES (v_svc, 3, true, false,
    'DIESEL', 'SEMI_AUTOMATIC');
  INSERT INTO available_times (service_id, day_of_week, open_time, close_time, is_24_hours, is_closed) VALUES
    (v_svc, 'MONDAY', '06:00', '22:00', false, false),
    (v_svc, 'TUESDAY', '06:00', '22:00', false, false),
    (v_svc, 'WEDNESDAY', '06:00', '22:00', false, false),
    (v_svc, 'THURSDAY', '06:00', '22:00', false, false),
    (v_svc, 'FRIDAY', '06:00', '22:00', false, false),
    (v_svc, 'SATURDAY', '06:00', '22:00', false, false),
    (v_svc, 'SUNDAY', '06:00', '22:00', false, false);
  INSERT INTO image (service_id, image_url) VALUES (v_svc, '/uploads/service-pictures/demo-galle-tours-1.svg');

  -- Policies
  INSERT INTO policy_section (heading, policy, category_id, provider_id) VALUES ('Cancellation Policy', '<p>No refund will be provided in case of any cancellation</p>', 4, v_provider)
  RETURNING id INTO v_policy;
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'1')::bigint, v_policy);
  INSERT INTO policy_section (heading, policy, category_id, provider_id) VALUES ('Check-in & Check-out', '<p>Check-in from 2:00 PM, check-out by 11:00 AM. Early check-in is subject to availability.</p>', 1, v_provider)
  RETURNING id INTO v_policy;
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'104')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'105')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'208')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'253')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'306')::bigint, v_policy);
  INSERT INTO policy_section (heading, policy, category_id, provider_id) VALUES ('Cancellation Policy', '<p>Free cancellation up to 48 hours before pick-up. Later cancellations are charged 50%.</p>', 2, v_provider)
  RETURNING id INTO v_policy;
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'102')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'203')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'204')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'205')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'307')::bigint, v_policy);
  INSERT INTO service_policy (service_id, policy_id) VALUES ((s->>'353')::bigint, v_policy);

  -- Past trip with completed, paid bookings (feeds revenue and analytics)
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Galle', 'Sri Lanka', 'Galle', 'Southern Province', 'Wakwella Rd, Galle 80000, Sri Lanka',
    '80000', 6.032894799999999, 80.2167912, ST_SetSRID(ST_MakePoint(80.2167912, 6.032894799999999), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO trips (trip_name, trip_status, start_date, end_date, number_of_adults, number_of_children, total_budget, total_budget_limit,
    total_distance, start_location_id)
  VALUES ('Southern Coast Weekend', 'COMPLETED', current_date - 45, current_date - 45 + 3, 2, 0, 120000, 120000, 0, v_loc) RETURNING trip_id INTO v_trip;
  INSERT INTO trip_participants (trip_id, tourist_id, trip_role, joined_at) VALUES (v_trip, v_tourist, 'ADMIN', now() - interval '60 days')
  RETURNING participant_id INTO v_part;
  INSERT INTO trip_participants (trip_id, tourist_id, trip_role, invited_by_tourist_id, joined_at)
  VALUES (v_trip, v_tourist2, 'MEMBER', v_tourist, now() - interval '59 days') RETURNING participant_id INTO v_part2;
  INSERT INTO chat_rooms (type, created_at) VALUES ('GROUP', now() - interval '60 days') RETURNING room_id INTO v_room;
  INSERT INTO group_chat_room (room_id, trip_id) VALUES (v_room, v_trip);
  INSERT INTO group_chat_room_participants (room_id, participant_id) VALUES (v_room, v_part), (v_room, v_part2);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'TRANSPORT', 20000, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'FOOD', 20000, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'ACCOMMODATION', 20000, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'ACTIVITY', 20000, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'SHOPPING', 20000, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'MISCELLANEOUS', 20000, 0);
  INSERT INTO trip_items (trip_id, service_id, trip_item_type, start_time, end_time, no_of_units, number_of_adults, number_of_children)
  VALUES (v_trip, (s->>'208')::bigint, 'SERVICE', (current_date - 45)::timestamp + time '14:00', (current_date - 45 + 2)::timestamp + time '11:00', 1, 2, 0) RETURNING trip_item_id INTO v_item;
  INSERT INTO bookings (trip_item_id, trip_participant_id, booking_status, booked_date_time, start_date_time, end_date_time,
    total_price, paid_amount, deposit_amount, updated_at)
  VALUES (v_item, v_part, 'CONFIRMED', (current_date - 45)::timestamp + time '14:00' - interval '10 days', (current_date - 45)::timestamp + time '14:00', (current_date - 45 + 2)::timestamp + time '11:00', 20000, 20000, 0, now())
  RETURNING booking_id INTO v_booking;
  INSERT INTO payments (booking_id, amount, commission, provider_amount, currency, status, payment_intent_id, stripe_charge_id, created_at)
  VALUES (v_booking, 20000, 2000.0, 18000.0, 'lkr', 'COMPLETED', 'pi_demo_001', 'ch_demo_001',
    (current_date - 45)::timestamp + time '14:00' - interval '10 days');
  v_complaint := v_booking;  -- remembered for the complaint below
  INSERT INTO trip_items (trip_id, service_id, trip_item_type, start_time, end_time, no_of_units, number_of_adults, number_of_children)
  VALUES (v_trip, (s->>'207')::bigint, 'SERVICE', (current_date - 45 + 1)::timestamp + time '19:00', (current_date - 45 + 1)::timestamp + time '20:30', 1, 2, 0) RETURNING trip_item_id INTO v_item;
  INSERT INTO bookings (trip_item_id, trip_participant_id, booking_status, booked_date_time, start_date_time, end_date_time,
    total_price, paid_amount, deposit_amount, updated_at)
  VALUES (v_item, v_part, 'CONFIRMED', (current_date - 45 + 1)::timestamp + time '19:00' - interval '10 days', (current_date - 45 + 1)::timestamp + time '19:00', (current_date - 45 + 1)::timestamp + time '20:30', 1500, 1500, 0, now())
  RETURNING booking_id INTO v_booking;
  INSERT INTO payments (booking_id, amount, commission, provider_amount, currency, status, payment_intent_id, stripe_charge_id, created_at)
  VALUES (v_booking, 1500, 150.0, 1350.0, 'lkr', 'COMPLETED', 'pi_demo_002', 'ch_demo_002',
    (current_date - 45 + 1)::timestamp + time '19:00' - interval '10 days');
  INSERT INTO trip_items (trip_id, service_id, trip_item_type, start_time, end_time, no_of_units, number_of_adults, number_of_children)
  VALUES (v_trip, (s->>'102')::bigint, 'SERVICE', (current_date - 45 + 2)::timestamp + time '09:00', (current_date - 45 + 2)::timestamp + time '18:00', 40, 2, 0) RETURNING trip_item_id INTO v_item;
  INSERT INTO bookings (trip_item_id, trip_participant_id, booking_status, booked_date_time, start_date_time, end_date_time,
    total_price, paid_amount, deposit_amount, updated_at)
  VALUES (v_item, v_part, 'CONFIRMED', (current_date - 45 + 2)::timestamp + time '09:00' - interval '10 days', (current_date - 45 + 2)::timestamp + time '09:00', (current_date - 45 + 2)::timestamp + time '18:00', 8000, 8000, 0, now())
  RETURNING booking_id INTO v_booking;
  INSERT INTO payments (booking_id, amount, commission, provider_amount, currency, status, payment_intent_id, stripe_charge_id, created_at)
  VALUES (v_booking, 8000, 800.0, 7200.0, 'lkr', 'COMPLETED', 'pi_demo_003', 'ch_demo_003',
    (current_date - 45 + 2)::timestamp + time '09:00' - interval '10 days');
  INSERT INTO trip_items (trip_id, service_id, trip_item_type, start_time, end_time, no_of_units, number_of_adults, number_of_children)
  VALUES (v_trip, (s->>'202')::bigint, 'SERVICE', (current_date - 45 + 1)::timestamp + time '07:00', (current_date - 45 + 1)::timestamp + time '10:00', 1, 2, 0) RETURNING trip_item_id INTO v_item;
  INSERT INTO bookings (trip_item_id, trip_participant_id, booking_status, booked_date_time, start_date_time, end_date_time,
    total_price, paid_amount, deposit_amount, updated_at)
  VALUES (v_item, v_part, 'CANCELLED', (current_date - 45 + 1)::timestamp + time '07:00' - interval '10 days', (current_date - 45 + 1)::timestamp + time '07:00', (current_date - 45 + 1)::timestamp + time '10:00', 4000, 0, 0, now())
  RETURNING booking_id INTO v_booking;

  -- Upcoming trip from the dump (Matara Explorer)
  INSERT INTO location (city, country, district, province, formatted_address, postal_code, latitude, longitude, coordinates)
  VALUES ('Unknown City', 'Sri Lanka', 'Gampaha District', 'Western Province', 'Canada Friendship Road , , Western Province',
    NULL, 7.167954042876612, 79.88288880934434, ST_SetSRID(ST_MakePoint(79.88288880934434, 7.167954042876612), 4326)::geography)
  RETURNING location_id INTO v_loc;
  INSERT INTO trips (trip_name, trip_status, start_date, end_date, number_of_adults, number_of_children, total_budget, total_budget_limit,
    total_distance, start_location_id)
  VALUES ('Matara Explorer', 'PLANNING', current_date + 10, current_date + 10 + 3, 2, 0, 60000, 60000, 0, v_loc) RETURNING trip_id INTO v_trip;
  INSERT INTO trip_participants (trip_id, tourist_id, trip_role, joined_at) VALUES (v_trip, v_tourist, 'ADMIN', now() - interval '60 days')
  RETURNING participant_id INTO v_part;
  INSERT INTO trip_participants (trip_id, tourist_id, trip_role, invited_by_tourist_id, joined_at)
  VALUES (v_trip, v_tourist2, 'MEMBER', v_tourist, now() - interval '59 days') RETURNING participant_id INTO v_part2;
  INSERT INTO chat_rooms (type, created_at) VALUES ('GROUP', now() - interval '60 days') RETURNING room_id INTO v_room;
  INSERT INTO group_chat_room (room_id, trip_id) VALUES (v_room, v_trip);
  INSERT INTO group_chat_room_participants (room_id, participant_id) VALUES (v_room, v_part), (v_room, v_part2);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'TRANSPORT', 0, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'ACTIVITY', 0, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'ACCOMMODATION', 0, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'SHOPPING', 10000, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'MISCELLANEOUS', 0, 0);
  INSERT INTO trip_budget_category (trip_id, budget_category, limit_amount, spent_amount) VALUES (v_trip, 'FOOD', 0, 0);
  INSERT INTO trip_items (trip_id, service_id, trip_item_type, start_time, end_time, no_of_units, number_of_adults, number_of_children)
  VALUES (v_trip, (s->>'1')::bigint, 'SERVICE', (current_date + 10)::timestamp + time '07:00', (current_date + 10)::timestamp + time '10:00', 1, 2, 0) RETURNING trip_item_id INTO v_item;
  INSERT INTO bookings (trip_item_id, trip_participant_id, booking_status, booked_date_time, start_date_time, end_date_time,
    total_price, paid_amount, deposit_amount, updated_at)
  VALUES (v_item, v_part, 'CONFIRMED', (current_date + 10)::timestamp + time '07:00' - interval '10 days', (current_date + 10)::timestamp + time '07:00', (current_date + 10)::timestamp + time '10:00', 45000, 45000, 0, now())
  RETURNING booking_id INTO v_booking;
  INSERT INTO payments (booking_id, amount, commission, provider_amount, currency, status, payment_intent_id, stripe_charge_id, created_at)
  VALUES (v_booking, 45000, 4500.0, 40500.0, 'lkr', 'COMPLETED', 'pi_demo_004', 'ch_demo_004',
    (current_date + 10)::timestamp + time '07:00' - interval '10 days');
  INSERT INTO trip_items (trip_id, service_id, trip_item_type, start_time, end_time, no_of_units, number_of_adults, number_of_children)
  VALUES (v_trip, (s->>'103')::bigint, 'SERVICE', (current_date + 10)::timestamp + time '18:30', (current_date + 10)::timestamp + time '20:00', 1, 2, 0) RETURNING trip_item_id INTO v_item;
  INSERT INTO bookings (trip_item_id, trip_participant_id, booking_status, booked_date_time, start_date_time, end_date_time,
    total_price, paid_amount, deposit_amount, updated_at)
  VALUES (v_item, v_part, 'PENDING', (current_date + 10)::timestamp + time '18:30' - interval '10 days', (current_date + 10)::timestamp + time '18:30', (current_date + 10)::timestamp + time '20:00', 7500, 0, 0, now())
  RETURNING booking_id INTO v_booking;
  INSERT INTO trip_items (trip_id, service_id, trip_item_type, start_time, end_time, no_of_units, number_of_adults, number_of_children)
  VALUES (v_trip, (s->>'306')::bigint, 'SERVICE', (current_date + 10 + 1)::timestamp + time '14:00', (current_date + 10 + 3)::timestamp + time '11:00', 1, 2, 0) RETURNING trip_item_id INTO v_item;

  -- Reviews
  INSERT INTO rate_review (service_id, tourist_id, rate, review, created_date)
  VALUES ((s->>'208')::bigint, v_tourist, 5, 'Beautiful resort, friendly staff and a great breakfast.', now() - interval '40 days');
  INSERT INTO rate_review (service_id, tourist_id, rate, review, created_date)
  VALUES ((s->>'207')::bigint, v_tourist, 4, 'Tasty rice and curry. Service was a little slow on a busy night.', now() - interval '41 days');
  INSERT INTO rate_review (service_id, tourist_id, rate, review, created_date)
  VALUES ((s->>'102')::bigint, v_tourist2, 5, 'Comfortable bus and a punctual driver for the whole day.', now() - interval '39 days');
  INSERT INTO rate_review (service_id, tourist_id, rate, review, created_date)
  VALUES ((s->>'1')::bigint, v_tourist2, 5, 'Saw a blue whale! The crew knew exactly where to go.', now() - interval '120 days');
  INSERT INTO rate_review (service_id, tourist_id, rate, review, created_date)
  VALUES ((s->>'253')::bigint, v_tourist2, 4, 'Peaceful lodge by the river, perfect for a quiet weekend.', now() - interval '150 days');

  -- Complaints: pending (paid booking, refundable), in progress, and resolved
  INSERT INTO complaint (complaint_id, service_id, tourist_id, booking_id, complaint_status, description, date_time)
  VALUES (nextval('complaint_seq'), (s->>'208')::bigint, v_tourist, v_complaint, 'PENDING',
    'The room was not cleaned on our second day and the air conditioning stopped working at night.', now() - interval '5 days')
  RETURNING complaint_id INTO v_complaint;
  INSERT INTO complaint_image (complaint_id, image_url) VALUES (v_complaint, '/uploads/complaint-images/demo-complaint-room.svg');
  INSERT INTO complaint (complaint_id, service_id, tourist_id, booking_id, complaint_status, description, date_time, investigation_started_date)
  VALUES (nextval('complaint_seq'), (s->>'102')::bigint, v_tourist2,
    (SELECT b.booking_id FROM bookings b JOIN trip_items ti ON ti.trip_item_id = b.trip_item_id WHERE ti.service_id = (s->>'102')::bigint LIMIT 1),
    'IN_PROGRESS', 'The bus arrived 40 minutes late for the morning pick-up.', now() - interval '6 days', (now() - interval '2 days')::text);
  INSERT INTO complaint (complaint_id, service_id, tourist_id, booking_id, complaint_status, complaint_result, description, date_time, admin_to_tourist)
  VALUES (nextval('complaint_seq'), (s->>'207')::bigint, v_tourist,
    (SELECT b.booking_id FROM bookings b JOIN trip_items ti ON ti.trip_item_id = b.trip_item_id WHERE ti.service_id = (s->>'207')::bigint LIMIT 1),
    'RESOLVED', 'REJECT', 'Our table was not ready at the booked time.', now() - interval '20 days',
    'We checked with the restaurant: the table was held for 30 minutes past the booking time, so no refund applies.');

  -- Direct chat room (messages are stored in MongoDB and start empty)
  INSERT INTO chat_rooms (type, created_at) VALUES ('DIRECT', now() - interval '50 days') RETURNING room_id INTO v_room;
  INSERT INTO direct_chat_room (room_id, provider_id, tourist_id) VALUES (v_room, v_provider, v_tourist);

  RAISE NOTICE 'Seeded LankaTrails demo data: 1 provider, 2 tourists, % services, 2 trips, bookings, reviews and complaints.',
    (SELECT count(*) FROM services WHERE provider_id = v_provider);
END
$seed$;

COMMIT;
