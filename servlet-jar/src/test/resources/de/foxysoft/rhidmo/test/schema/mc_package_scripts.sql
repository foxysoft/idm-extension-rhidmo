-- Surrogate definition of mc_package_scripts for unit tests based on H2 database.
-- Only those columns accessed by Rhidmo are included. Datatypes are specific to H2.
CREATE TABLE mc_package_scripts (
	mcPackageID int,
	mcScriptName nvarchar(128),
	mcScriptLanguage nvarchar(32),
	mcScriptDefinition text,
	mcEnabled tinyint
);
