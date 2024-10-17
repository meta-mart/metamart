-- Update the relation between testDefinition and testCase to 0 (CONTAINS)
UPDATE entity_relationship
SET relation = 0
WHERE fromEntity = 'testDefinition' AND toEntity = 'testCase' AND relation != 0;

-- Update the test definition provider
-- If the test definition has MetaMart as a test platform, then the provider is system, else it is user
UPDATE test_definition
SET json =
	case
		when json->'testPlatforms' @> '"MetaMart"' then jsonb_set(json,'{provider}','"system"',true)
		else jsonb_set(json,'{provider}','"user"', true)
	end;
