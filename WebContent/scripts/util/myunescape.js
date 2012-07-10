/*******************************************************************************

"myunescape ()", by Charlton Rose

Permission is granted to use and modifiy this script for any purpose,
provided that this credit header is retained, unmodified, in the script.

*******************************************************************************/


// This function is included to overcome a bug in Netscape's implementation
// of the escape () function:

function myunescape (str)
{
	str = "" + str;
	while (true)
	{
		var i = str . indexOf ('+');
		if (i < 0)
			break;
		str = str . substring (0, i) + '%20' +
			str . substring (i + 1, str . length);
	}
	return unescape (str);
}
