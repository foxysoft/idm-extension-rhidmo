-- Surrogate definition of mc_package for unit tests based on H2 database.
-- Only those columns accessed by Rhidmo are included. Datatypes are specific to H2.
CREATE TABLE mc_package (
	mcPackageID int,
	mcQualifiedName varchar(512)
);
