-- V202506300002__seed_catalog.sql
-- Catálogo inicial: 17 ServiceDefinitions
-- Precios orientativos; fórmulas en SpEL con variable 'property.price' y 'inputs'

SET search_path TO services;

INSERT INTO service_definitions
    (id, code, name, description, category, pricing_model, base_price, price_formula,
     sla_hours, workflow_key, inputs_schema, outputs_schema, requires_kyc, valid_for, status)
VALUES

-- 1. FIRST_HOME_AID ────────────────────────────────────────────────────────
(gen_random_uuid(), 'FIRST_HOME_AID',
 'Ayuda integral primer comprador',
 'Gestión 90+5+5, ICO, ayudas autonómicas y mediación hipotecaria incluida.',
 'FINANCING', 'FIXED', 0.00, NULL, 120,
 'first-home-aid',
 '{"type":"object","required":["customerId","propertyId"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"autonomousCommunity":{"type":"string"}}}',
 '{"type":"object","properties":{"preapprovalRef":{"type":"string"},"grantAmount":{"type":"number"},"summary":{"type":"string"}}}',
 TRUE, ARRAY['SALE'], 'ACTIVE'),

-- 2. MORTGAGE_BROKER ───────────────────────────────────────────────────────
(gen_random_uuid(), 'MORTGAGE_BROKER',
 'Mediación hipotecaria multi-banco',
 'Comparativa de hasta 12 entidades, negociación de condiciones y acompañamiento hasta firma.',
 'FINANCING', 'PERCENT_OF_PRICE', NULL, '0.009 * property.price',
 96, 'mortgage-broker',
 '{"type":"object","required":["customerId","propertyId"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"loanAmount":{"type":"number"}}}',
 '{"type":"object","properties":{"bestOfferBank":{"type":"string"},"tin":{"type":"number"},"tae":{"type":"number"},"offerRef":{"type":"string"}}}',
 TRUE, ARRAY['SALE'], 'ACTIVE'),

-- 3. BRIDGE_LOAN ───────────────────────────────────────────────────────────
(gen_random_uuid(), 'BRIDGE_LOAN',
 'Crédito puente compra-antes-de-venta',
 'Financiación transitoria para adquirir nueva vivienda antes de vender la actual.',
 'FINANCING', 'PERCENT_OF_PRICE', NULL, '0.012 * property.price',
 72, 'mortgage-broker',
 '{"type":"object","required":["customerId","propertyId","currentPropertyId"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"currentPropertyId":{"type":"string","format":"uuid"}}}',
 '{"type":"object","properties":{"loanRef":{"type":"string"},"bridgeAmount":{"type":"number"}}}',
 TRUE, ARRAY['SALE'], 'ACTIVE'),

-- 4. RENT_DEFAULT_INSURANCE ────────────────────────────────────────────────
(gen_random_uuid(), 'RENT_DEFAULT_INSURANCE',
 'Seguro de impago de alquiler',
 'Cobertura impago hasta 12 meses (Mapfre, AXA, Mutua de Propietarios, ARAG).',
 'INSURANCE', 'PERCENT_OF_PRICE', NULL, '0.035 * inputs.annualRent',
 48, 'rent-default-insurance',
 '{"type":"object","required":["customerId","propertyId","tenantId","annualRent"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"tenantId":{"type":"string","format":"uuid"},"annualRent":{"type":"number"},"monthsCoverage":{"type":"integer","default":12},"fraudClause":{"type":"boolean","default":false}}}',
 '{"type":"object","properties":{"policyNumber":{"type":"string"},"insurer":{"type":"string"},"coverageMonths":{"type":"integer"},"policyDoc":{"type":"string","format":"uri"}}}',
 TRUE, ARRAY['RENT'], 'ACTIVE'),

-- 5. HOME_INSURANCE ────────────────────────────────────────────────────────
(gen_random_uuid(), 'HOME_INSURANCE',
 'Seguro de hogar',
 'Seguro multirriesgo de hogar, vinculable a hipoteca.',
 'INSURANCE', 'FIXED', 180.00, NULL,
 48, 'rent-default-insurance',
 '{"type":"object","required":["customerId","propertyId"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"linkToMortgage":{"type":"boolean","default":false}}}',
 '{"type":"object","properties":{"policyNumber":{"type":"string"},"insurer":{"type":"string"},"annualPremium":{"type":"number"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 6. LIFE_INSURANCE ────────────────────────────────────────────────────────
(gen_random_uuid(), 'LIFE_INSURANCE',
 'Seguro de vida',
 'Seguro de vida con capital equivalente al préstamo hipotecario, vinculable.',
 'INSURANCE', 'MONTHLY_SUBSCRIPTION', NULL, '0.0003 * inputs.capitalAmount',
 48, 'rent-default-insurance',
 '{"type":"object","required":["customerId","capitalAmount"],"properties":{"customerId":{"type":"string","format":"uuid"},"capitalAmount":{"type":"number"},"linkToMortgage":{"type":"boolean","default":false}}}',
 '{"type":"object","properties":{"policyNumber":{"type":"string"},"insurer":{"type":"string"},"monthlyPremium":{"type":"number"}}}',
 FALSE, ARRAY['SALE'], 'ACTIVE'),

-- 7. RENT_GUARANTEE ────────────────────────────────────────────────────────
(gen_random_uuid(), 'RENT_GUARANTEE',
 'Aval bancario o fianza para alquiler',
 'Gestión de aval bancario o constitución de fianza adicional para propietarios exigentes.',
 'LEGAL', 'FIXED', 250.00, NULL,
 72, 'notary-gestoria',
 '{"type":"object","required":["customerId","propertyId","guaranteeAmount"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"guaranteeAmount":{"type":"number"}}}',
 '{"type":"object","properties":{"guaranteeDoc":{"type":"string","format":"uri"},"bankRef":{"type":"string"}}}',
 TRUE, ARRAY['RENT'], 'ACTIVE'),

-- 8. APPRAISAL ─────────────────────────────────────────────────────────────
(gen_random_uuid(), 'APPRAISAL',
 'Tasación ECO 805/2003',
 'Tasación oficial homologada por tasadora inscrita en el Banco de España.',
 'TECHNICAL', 'FIXED', 350.00, NULL,
 48, 'appraisal',
 '{"type":"object","required":["customerId","propertyId"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"urgency":{"type":"string","enum":["NORMAL","EXPRESS"]}}}',
 '{"type":"object","properties":{"appraisalValue":{"type":"number"},"reportDoc":{"type":"string","format":"uri"},"appraisalRef":{"type":"string"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 9. PROFILE_SEARCH ────────────────────────────────────────────────────────
(gen_random_uuid(), 'PROFILE_SEARCH',
 'Búsqueda profesional de perfil inquilino',
 'Selección y entrega de shortlist verificada de inquilinos candidatos (KYC + solvencia).',
 'MANAGEMENT', 'FIXED', 299.00, NULL,
 96, 'profile-search',
 '{"type":"object","required":["customerId","propertyId","minIncomeMultiple"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"minIncomeMultiple":{"type":"number","minimum":1},"contractType":{"type":"string"},"maxDependents":{"type":"integer"},"requiresGuarantor":{"type":"boolean"},"petsAllowed":{"type":"boolean"},"targetMoveInDate":{"type":"string","format":"date"}}}',
 '{"type":"object","properties":{"shortlist":{"type":"array","items":{"type":"string","format":"uuid"}}}}',
 FALSE, ARRAY['RENT'], 'ACTIVE'),

-- 10. PROPERTY_SEARCH ──────────────────────────────────────────────────────
(gen_random_uuid(), 'PROPERTY_SEARCH',
 'Personal shopper inmobiliario',
 'Búsqueda activa y curada de inmuebles según criterios del cliente comprador o arrendatario.',
 'MANAGEMENT', 'FIXED', 399.00, NULL,
 120, 'property-search',
 '{"type":"object","required":["customerId","targetTicket","zoneIds"],"properties":{"customerId":{"type":"string","format":"uuid"},"targetTicket":{"type":"number"},"zoneIds":{"type":"array","items":{"type":"string","format":"uuid"}},"propertyTypes":{"type":"array","items":{"type":"string"}},"mustHaves":{"type":"array"},"niceToHaves":{"type":"array"},"deadline":{"type":"string","format":"date"}}}',
 '{"type":"object","properties":{"shortlist":{"type":"array","items":{"type":"string","format":"uuid"}}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 11. LEGAL_REVIEW ─────────────────────────────────────────────────────────
(gen_random_uuid(), 'LEGAL_REVIEW',
 'Revisión jurídica de contratos',
 'Análisis de nota simple, cargas, contrato de compraventa o arrendamiento por letrado colegiado.',
 'LEGAL', 'FIXED', 199.00, NULL,
 72, 'notary-gestoria',
 '{"type":"object","required":["customerId","propertyId","documentType"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"documentType":{"type":"string","enum":["PURCHASE_CONTRACT","RENTAL_CONTRACT","NOTA_SIMPLE","CHARGES_REPORT"]}}}',
 '{"type":"object","properties":{"reportDoc":{"type":"string","format":"uri"},"issues":{"type":"array"},"recommendation":{"type":"string"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 12. TECHNICAL_REPORT ─────────────────────────────────────────────────────
(gen_random_uuid(), 'TECHNICAL_REPORT',
 'ITE / Informe técnico / Certificado energético',
 'Inspección técnica del edificio, informe de estado y certificado de eficiencia energética.',
 'TECHNICAL', 'FIXED', 275.00, NULL,
 96, 'appraisal',
 '{"type":"object","required":["customerId","propertyId","reportType"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"reportType":{"type":"string","enum":["ITE","TECHNICAL_STATE","ENERGY_CERTIFICATE","FULL"]}}}',
 '{"type":"object","properties":{"reportDoc":{"type":"string","format":"uri"},"energyRating":{"type":"string"},"defectsFound":{"type":"integer"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 13. RENOVATION_QUOTE ─────────────────────────────────────────────────────
(gen_random_uuid(), 'RENOVATION_QUOTE',
 'Presupuesto de reforma con partners',
 'Visita de arquitecto/reformista partner y entrega de presupuesto detallado.',
 'RENOVATION', 'FIXED', 0.00, NULL,
 72, 'appraisal',
 '{"type":"object","required":["customerId","propertyId","renovationScope"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"renovationScope":{"type":"string","enum":["KITCHEN","BATHROOM","FULL","PARTIAL","ENERGY_UPGRADE"]},"budget":{"type":"number"}}}',
 '{"type":"object","properties":{"quoteDoc":{"type":"string","format":"uri"},"estimatedAmount":{"type":"number"},"durationWeeks":{"type":"integer"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 14. MOVING ───────────────────────────────────────────────────────────────
(gen_random_uuid(), 'MOVING',
 'Mudanza con socios certificados',
 'Gestión completa de mudanza con empresa certificada: embalaje, transporte y montaje.',
 'LOGISTICS', 'QUOTE_BASED', NULL, NULL,
 48, 'appraisal',
 '{"type":"object","required":["customerId","originAddress","destinationAddress"],"properties":{"customerId":{"type":"string","format":"uuid"},"originAddress":{"type":"string"},"destinationAddress":{"type":"string"},"movingDate":{"type":"string","format":"date"},"volume":{"type":"string","enum":["SMALL","MEDIUM","LARGE","EXTRA_LARGE"]}}}',
 '{"type":"object","properties":{"quoteAmount":{"type":"number"},"movingCompany":{"type":"string"},"confirmationDoc":{"type":"string","format":"uri"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 15. UTILITIES_SETUP ──────────────────────────────────────────────────────
(gen_random_uuid(), 'UTILITIES_SETUP',
 'Alta de suministros',
 'Gestión de altas de electricidad, agua, gas e internet en el nuevo domicilio.',
 'UTILITIES', 'FIXED', 89.00, NULL,
 48, 'appraisal',
 '{"type":"object","required":["customerId","propertyId"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"utilities":{"type":"array","items":{"type":"string","enum":["ELECTRICITY","WATER","GAS","INTERNET"]},"default":["ELECTRICITY","WATER","GAS","INTERNET"]},"moveInDate":{"type":"string","format":"date"}}}',
 '{"type":"object","properties":{"electricityRef":{"type":"string"},"waterRef":{"type":"string"},"gasRef":{"type":"string"},"internetRef":{"type":"string"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 16. TAX_ADVISORY ─────────────────────────────────────────────────────────
(gen_random_uuid(), 'TAX_ADVISORY',
 'Asesoría fiscal de la operación',
 'Cálculo y optimización de IRPF, plusvalía municipal, ITP/IVA por asesor fiscal colegiado.',
 'LEGAL', 'FIXED', 299.00, NULL,
 72, 'notary-gestoria',
 '{"type":"object","required":["customerId","propertyId","operationType"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"operationType":{"type":"string","enum":["SALE","RENT","PURCHASE"]},"transactionAmount":{"type":"number"}}}',
 '{"type":"object","properties":{"reportDoc":{"type":"string","format":"uri"},"estimatedTax":{"type":"number"},"taxOptimizations":{"type":"array"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE'),

-- 17. NOTARY_GESTORIA ──────────────────────────────────────────────────────
(gen_random_uuid(), 'NOTARY_GESTORIA',
 'Coordinación notaría + gestoría',
 'Reserva de cita notarial, preparación de documentación y liquidación de impuestos vía gestoría.',
 'LEGAL', 'FIXED', 450.00, NULL,
 72, 'notary-gestoria',
 '{"type":"object","required":["customerId","propertyId","operationType"],"properties":{"customerId":{"type":"string","format":"uuid"},"propertyId":{"type":"string","format":"uuid"},"operationType":{"type":"string","enum":["SALE","RENT","MORTGAGE"]},"preferredNotaryDate":{"type":"string","format":"date-time"}}}',
 '{"type":"object","properties":{"notaryAppointment":{"type":"string","format":"date-time"},"notaryAddress":{"type":"string"},"deedDoc":{"type":"string","format":"uri"},"gestoriaRef":{"type":"string"}}}',
 FALSE, ARRAY['SALE','RENT'], 'ACTIVE');
