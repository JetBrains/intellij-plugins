<cfscript>
  array2 = [1, 2, 3, 4, 5, 6, 7, 8];
  newArray = arrayMap(array2, function(item, index, arrayp) {});
  arrayReduce(array2, function(result, item) {
    result = result?:0;
    return result + item;
  }, 1);
  deserialize("foo:bar", "json");
  deserializeXml("<foo/>", true);
</cfscript>
<cffunction access="remote" name="canDeSerialize" returntype="boolean">

  <cfargument name="type" type="string"/>

<!--- If you need to deserialize only XML using the custom serializer. --->
  <cfif #type# eq "XML">

    <cfreturn true>

  <cfelse>

    <cfreturn false>

  </cfif>

</cffunction>
<cffunction access="remote" name="canSerialize" returntype="boolean">

  <cfargument name="type" type="string"/>

<!--- If you need to serialize only XML using the custom serializer. --->
  <cfif #type# eq "XML">

    <cfreturn true>

  <cfelse>

    <cfreturn false>

  </cfif>

</cffunction>

<!---- Prints ,.-_ ---->
<cfset x=encodeForXMLAttribute(",.-_")>
<cfoutput>#x#</cfoutput>


<cfscript>
  salt="A41n9t0Q";
  password = "Password@123";
  PBKDFalgorithm = "PBKDF2WithSHA224";
  dataToEncrypt= "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua";
  encryptionAlgorithm = "AES";
  derivedKey = GeneratePBKDFKey(PBKDFalgorithm ,password ,salt,4096,128);
  writeOutput("Generated PBKDFKey (Base 64) : " & derivedKey);
  encryptedData = encrypt(dataToEncrypt, derivedKey, encryptionAlgorithm, "BASE64");
  writeoutput("Data After Encryption using PBKDF2: " & encryptedData);
</cfscript>

<cfscript>
  salt="A41n9t0Q";
  password = "Password@123";
  PBKDFalgorithm = "PBKDF2WithSHA224";
  derivedKey = GeneratePBKDFKey(PBKDFalgorithm ,password ,salt,4096,128);
  decryptedData = decrypt(encryptedData, derivedKey, encryptionAlgorithm, "BASE64");
  writeoutput("Data After Decryption using PBKDF2: " & decryptedData);
  inputHtml = "";
</cfscript>
<!---- Prints &#x27;or 1&#x3d;1---->

<cfset x=encodeForXPath("'or 1=1",false)>
<cfoutput>#x#</cfoutput>

<cfset  isSafe = isSafeHTML(inputHTML)>
<cfset  SafeHTML = getSafeHTML(inputHTML, "", true)>
<cfoutput> is Safe   : #isSafe#  Safe HTML : #SafeHTML#  </cfoutput>