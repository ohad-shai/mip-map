package com.ohadshai.mipmap.entities.ip_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an IP location, which holds the location details.
 * For example:
 * {
 * "as": "AS12849 AMS-IX Admin LAN",
 * "city": "Rishon LeZion",
 * "country": "Israel",
 * "countryCode": "IL",
 * "isp": "HOTnet",
 * "lat": 31.9642,
 * "lon": 34.8044,
 * "org": "HOTnet",
 * "query": "37.142.70.97",
 * "region": "M",
 * "regionName": "Central District",
 * "status": "success",
 * "timezone": "Asia/Jerusalem",
 * "zip": ""
 * }
 * Created by Ohad on 12/14/2016.
 */
public class IPLocation implements Parcelable {

    //region Private Members

    /**
     * Holds the IP address of the location.
     */
    @SerializedName("query")
    private String ip;

    /**
     * Holds the latitude of the IP location.
     */
    @SerializedName("lat")
    private double latitude;

    /**
     * Holds the longitude of the IP location.
     */
    @SerializedName("lon")
    private double longitude;

    /**
     * Holds the city of the IP location.
     */
    private String city;

    /**
     * Holds the country of the IP location.
     */
    private String country;

    /**
     * Holds the country ISO2 code of the IP location.
     */
    private String countryCode;

    /**
     * Holds the isp of the IP location.
     */
    private String isp;

    /**
     * Holds the organization of the IP location.
     */
    private String org;

    /**
     * Holds the region of the IP location.
     */
    private String region;

    /**
     * Holds the region name of the IP location.
     */
    private String regionName;

    /**
     * Holds the timezone of the IP location.
     */
    private String timezone;

    /**
     * Holds the zip code of the IP location.
     */
    private String zip;

    //endregion

    //region Public API

    /**
     * Gets the IP address of the location.
     *
     * @return Returns the IP address of the location.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Sets the IP address of the location.
     *
     * @param ip The IP address of the location to set.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Gets the latitude of the IP location.
     *
     * @return Returns the latitude of the IP location.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude of the IP location.
     *
     * @param latitude The latitude of the IP location to set.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude of the IP location.
     *
     * @return Returns the longitude of the IP location.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude of the IP location.
     *
     * @param longitude The longitude of the IP location to set.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets the city of the IP location.
     *
     * @return Returns the city of the IP location.
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city of the IP location.
     *
     * @param city The city of the IP location to set.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the country of the IP location.
     *
     * @return Returns the country of the IP location.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country of the IP location.
     *
     * @param country The country of the IP location to set.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the country ISO2 code of the IP location.
     *
     * @return Returns the country ISO2 code of the IP location.
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the country ISO2 code of the IP location.
     *
     * @param countryCode The country ISO2 code of the IP location to set.
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Gets the isp of the IP location.
     *
     * @return Returns the isp of the IP location.
     */
    public String getIsp() {
        return isp;
    }

    /**
     * Sets the isp of the IP location.
     *
     * @param isp The isp of the IP location to set.
     */
    public void setIsp(String isp) {
        this.isp = isp;
    }

    /**
     * Gets the org of the IP location.
     *
     * @return Returns the org of the IP location.
     */
    public String getOrg() {
        return org;
    }

    /**
     * Sets the org of the IP location.
     *
     * @param org The org of the IP location to set.
     */
    public void setOrg(String org) {
        this.org = org;
    }

    /**
     * Gets the region of the IP location.
     *
     * @return Returns the region of the IP location.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region of the IP location.
     *
     * @param region The region of the IP location to set.
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets the region name of the IP location.
     *
     * @return Returns the region name of the IP location.
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * Sets the region name of the IP location.
     *
     * @param regionName The region name of the IP location to set.
     */
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    /**
     * Gets the timezone of the IP location.
     *
     * @return Returns the timezone of the IP location.
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Sets the timezone of the IP location.
     *
     * @param timezone The timezone of the IP location to set.
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * Gets the zip code of the IP location.
     *
     * @return Returns the zip code of the IP location.
     */
    public String getZip() {
        return zip;
    }

    /**
     * Sets the zip code of the IP location.
     *
     * @param zip The zip code of the IP location to set.
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    //endregion

    //region (Parcelable)

    protected IPLocation(Parcel in) {
        ip = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        city = in.readString();
        country = in.readString();
        countryCode = in.readString();
        isp = in.readString();
        org = in.readString();
        region = in.readString();
        regionName = in.readString();
        timezone = in.readString();
        zip = in.readString();
    }

    public static final Creator<IPLocation> CREATOR = new Creator<IPLocation>() {
        @Override
        public IPLocation createFromParcel(Parcel in) {
            return new IPLocation(in);
        }

        @Override
        public IPLocation[] newArray(int size) {
            return new IPLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ip);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(city);
        parcel.writeString(country);
        parcel.writeString(countryCode);
        parcel.writeString(isp);
        parcel.writeString(org);
        parcel.writeString(region);
        parcel.writeString(regionName);
        parcel.writeString(timezone);
        parcel.writeString(zip);
    }

    //endregion

}
