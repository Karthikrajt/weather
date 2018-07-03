package report.weather.karthikraj.weather;

/**
 * Created by KarthikT on 7/2/2018.
 */

public class city_weather_details {
    int city_id;
    String city_name;
    double temp;
    double temp_min;
    double temp_max;
    double pressure;
    double humidity;
    double wind_speed;
    double wind_deg;
    String weather_main;
    String weather_description;
    String icon;

    public city_weather_details()
    {

    }

    public city_weather_details(int city_id,
            String city_name,
            double temp,
            double temp_min,
            double temp_max,
            double pressure,
            double humidity,
            double wind_speed,
            double wind_deg,
            String weather_main,
            String weather_description,
            String icon)
    {
        this.city_id= city_id;
        this.city_name= city_name;
        this.temp = temp;
        this.temp_min= temp_min;
        this.temp_max= temp_max;
        this.pressure = pressure;
        this.humidity = humidity;
        this.wind_speed = wind_speed;
        this.wind_deg = wind_deg;
        this.weather_main = weather_main;
        this.weather_description = weather_description;
        this.icon = icon;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public double getTemp() {
        return temp;
    }

    public double getTemp_max() {
        return temp_max;
    }

    public double getTemp_min() {
        return temp_min;
    }

    public double getWind_deg() {
        return wind_deg;
    }

    public double getWind_speed() {
        return wind_speed;
    }

    public int getCity_id() {
        return city_id;
    }

    public String getCity_name() {
        return city_name;
    }

    public String getIcon() {
        return icon;
    }


    public String getWeather_description() {
        return weather_description;
    }

    public String getWeather_main() {
        return weather_main;
    }

}
