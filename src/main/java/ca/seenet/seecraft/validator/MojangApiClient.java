package ca.seenet.seecraft.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MojangApiClient
{
    private static String DEFAULT_ENDPOINT_BASE = "https://api.mojang.com/";

    private static Pattern ILLEGAL_USERNAME_PATTERN = Pattern.compile("[^A-Za-z0-9_]");
    private static Pattern UUID_PATTERN = Pattern.compile("[a-z0-9]{32}");

    private String endpointBase;

    public MojangApiClient()
    {
        this(DEFAULT_ENDPOINT_BASE);
    }

    public MojangApiClient(String endpointBase)
    {
        this.endpointBase = endpointBase;
    }

    public String getPlayerUuid(String username) throws IllegalArgumentException, IOException
    {
        if (ILLEGAL_USERNAME_PATTERN.matcher(username).matches())
        {
            throw new IllegalArgumentException("The username contains illegal characters.");
        }

        URL endpoint = null;
        try
        {
            endpoint = new URL(endpointBase + "users/profiles/minecraft/" + username);
        }
        catch (MalformedURLException e)
        {
            /* Boourns. */
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(endpoint.openConnection().getInputStream()));
        String response = reader.readLine();

        Matcher uuidMatcher = UUID_PATTERN.matcher(response);
        if (!uuidMatcher.find())
        {
            throw new IllegalArgumentException("Failure retrieving UUID for player");
        }

        String uuid = uuidMatcher.group();
        return uuid;
    }
}
