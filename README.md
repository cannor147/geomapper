# GeoMapper
Simply, this is a lightweight tool for creating colored map, based on countries (or territories) data.
It has three ways of colorization.

## Straight mode
Use it for colorization of country sets in some colors. Below is an example of 7 group of countries with similar geographic and cultural background.

![Straight mode example](example/straight.png "Straight mode")

For the same result use this command:
`
countries
-use straight
-from "src/main/resources/example/groups.csv"
-list 0 blue
-list 1 red
-list 2 orange
-list 3 yellow
-list 4 green
-list 5 fuchsia
-list 6 teal
`

## Scale mode
Use it for colorization of countries based on value (gdp, hdi, population, etc.). Below is an example of countries by GDP (PPP) per capita.

![Scale mode example](example/scale.png "Scale mode")

For the same result use this command:
`
countries
-use scale -color green -logarithmization 10
-fromValues "src/main/resources/example/gdp.csv" 1 2
`

## Step mode
Use it for colorization of countries based on value (gdp, hdi, population, etc.), when you want to group some values. For example, you can color countries with population between 10M and 100M in one color. Below is an example of countries by HDI.

![Step mode example](example/step.png "Scale mode")

For the same result use this command:
`
countries
-use step -colors blue red -separators 3 0.8 0.7 0.55
-fromValues "src/main/resources/example/hdi.csv" 2 3
`

## Custom maps
You can use this tool not only for the map of countries. Below is an example of Russian subjects by population (except Crimea and Sevastopol). 

![Russian map example](example/russia.png "Russian map")

For the same result use this command:
`
russia
-use scale -color lime -logarithmization 2
-fromValues "src/main/resources/example/rus-population.csv" 2 6
`

And here is one more example of USA states ranked by proportion of African American population

![USA map example](example/usa.png "USA map")

For the same result use this command:
`
usa
-use scale -colors brown pale_yellow -min 0 -max 100
-fromValues "src/main/resources/example/usa-african-american.csv" 2 0
`
