package de.amirrocker.responsivearrowplayground.scratchpad

import arrow.optics.Lens
import arrow.optics.copy
import arrow.optics.optics
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import java.util.Locale

@optics
@JvmInline
value class Name(val value: String) {
    companion object
}

@optics
@JvmInline
value class Age(val value: String) {
    companion object
}

@optics
@JvmInline
value class HouseNumber(val value: String) {
    companion object
}

@optics
@JvmInline
value class Country(val value: String) {
    companion object
}

/*
                      ↱ this lens operates on 'Customer'
    val address: Lens<Customer, Address>
                              ↳ this lens gives access to an 'Address' value

    Lenses provide three primary operations:

    get -> obtains the elements focused on a lens.
    set -> changes the value of the focus to a new one.
    modify -> transforms the value of the focus by applying a given function.

 */
@optics
data class Customer(
    val name: Name,
    val age: Age,
    val address: Address
) {
    companion object {
        fun with(
            name: Name = Name("kevin"),
            age: Age = Age("12"),
            address: Address = Address.with(Street.with(), City.with())
        ) = Customer(
            name = name,
            age = age,
            address = address
        )
    }
}

@optics
data class Address(
    val street: Street,
    val city: City
) {
    companion object {
        fun with(
            street: Street = Street.with(),
            city: City = City.with()
        ) = Address(
            street = street,
            city = city
        )
    }
}

@optics
data class Street(
    val name: Name,
    val houseNumber: HouseNumber
) {
    companion object {
        fun with(
            name: Name = Name("mastreet"),
            houseNumber: HouseNumber = HouseNumber("27/1 B")
        ) = Street(
            name = name,
            houseNumber = houseNumber
        )
    }
}

@optics
data class City(
    val name: Name,
    val state: State,
    val country: Country
) {
    companion object {
        fun with(
            name: Name = Name("CologneCity"),
            state: State = State.NRW,
            country: Country = Country(CountryEnum.GERMANY.name)
        ) = City(
            name = name,
            state = state,
            country = country
        )
    }
}

enum class State {
    NRW, BW, HH
}

enum class CountryEnum {
    GERMANY, TURKEY, FRANCE
}

// now use the optics to access nested obj props
// using Optic-first
internal fun Customer.capitalizeCountryModify(): Customer =
    Customer.address.city.country.value.modify(this) {
        it.lowercase().replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.GERMANY)
            } else it.toString()
        }
    }

// using Copy builder
internal fun Customer.lowerCaseCityName(): Customer =
    this.copy {
        Customer.address.city.name.value transform { it.lowercase() }
    }

// test object to work on
internal val customerA = Customer.with(
    Name("kevin"),
    Age("12"),
    Address.with(
        Street(
            Name("mastreet"),
            HouseNumber("27/1 B")
        ),
        City(
            Name("CologneCity"),
            State.NRW,
            country = Country(CountryEnum.GERMANY.name)
        )
    )
)

internal val customerB = Customer.with(
    Name("Shahntalle"),
    Age("38"),
    Address(
        Street(
            Name("mastreet"),
            HouseNumber("27/1 B")
        ),
        City(
            Name("CologneCity"),
            State.NRW,
            country = Country(CountryEnum.GERMANY.name)
        )
    )
)

fun testBasicOptics() {

    val modifiedCityCustomerA = customerA.lowerCaseCityName()
    assert(modifiedCityCustomerA.address.city.name.value == "colognecity") { "expect colognecity but was $modifiedCityCustomerA" }.also {
        println("modifiedCityCustomerA is $modifiedCityCustomerA")
    }

    val modifiedCountryCustomerA = customerA.capitalizeCountryModify()
    assert(modifiedCountryCustomerA.address.city.country.value == "Germany") { "expect Germany but was $modifiedCountryCustomerA" }.also {
        println("modifiedCountryCustomerA is $modifiedCountryCustomerA")
    }
}

fun testLensAccessors() {
    // a 'regular' setter operating on the lense
    // note how the instance to operate on is passed as an argument
    Customer.name.get(customerA).value shouldBeEqualTo Name("kevin").value

    val modifiedStreet = Customer.address.street.name.value.modify(customerA) { it.uppercase() }
    assert(modifiedStreet.address.street.name.value == "MASTREET") { "expect MASTREET but was $modifiedStreet" }.also {
        println("modifiedStreet is $modifiedStreet")
    }

    val withNewAddress = Customer.address.set(
        customerA, Address.with(
            Street(
                Name("somestreet"),
                HouseNumber("99/2 C")
            ),
            City(
                Name("Hinterfuddelhausen"),
                State.BW,
                country = Country(CountryEnum.TURKEY.name)
            )
        )
    )

    withNewAddress.address.street.name.value shouldBe "somestreet"
}

// now comes the fun part
// composition: The power of lenses and optics lies in the ability to compose them to get to values
fun testComposition() {
    val customerCityString: Lens<Customer, String> =
        Customer.address compose Address.city compose City.name.value

    customerCityString.get(customerA) shouldBeEqualTo "CologneCity"
    val meAfterRelocation = customerCityString.set(customerA, "Rome")
    meAfterRelocation.address.city.name.value shouldBe "Rome"
}

fun Customer.cityName():String = (Customer.address compose Address.city compose City.name.value).get(this)

fun Customer.name():String = (Customer.name compose Name.value).get(this)

fun Customer.countryName():String = (Customer.address compose Address.city compose City.country.value).get(this)

fun Customer.country(): Country = (Customer.address compose Address.city compose City.country).get(this)

fun Customer.age(): Int = (Customer.age.value).get(this).toInt()

fun testSimpleExtensions() {
    Customer.with(address = Address.with(city = City.with(name = Name("New York"))))
        .cityName() shouldBe "New York"
    customerB.name() shouldBe "Shahntalle"
    Customer.with()
        .countryName() shouldBe CountryEnum.GERMANY.name

    Customer.with().country() shouldBeEqualTo Country(CountryEnum.GERMANY.name)
}

// modify multiple nested values
// using the regular set method but nested. Ok, but not ideal
fun Customer.relocateToDifferentCity(): Customer =
    Customer.address.city.name.value.set(
        Customer.address.city.country.value.set(this, "Spain"),
        "Barcelona"
    )

fun Customer.relocateToCityAndCountry(city:City, country: Country): Customer =
    Customer.address.city.name.value.set(
        Customer.address.city.country.value.set(this, country.value),

        city.name.value
    )

// using the enhanced copy method
fun Customer.marryAndRelocate(
    cityName: String = "Paris",
    streetName:String = "Rue de Shantalle",
    houseNumber:String = "Carré Cinque, 32f",
    name:String = "Madame Voltaire",
): Customer = copy {
    Customer.address.city.name.value set cityName
    Customer.address.street.name.value set streetName
    Customer.address.street.houseNumber.value set houseNumber
    Customer.name.value set name
}

// or using copy with inside to remove the duplication
fun Customer.turn18AndMoveOut() = copy {
    Customer.age.value set "18"
    inside(Customer.address.city) {
        City.name.value set "Rio de Janeiro"
        City.country.value set "Argentine"
    }
}

fun Customer.turn18ChangeNameAndMoveOut(
    age:String = "18",
    name: String = "Schneewitchen",
    cityName: String = "Rio de Janeiro",
    country: Country = Country(value = "Argentine")
) = copy {
    Customer.age.value set age
    Customer.name.value set name
    inside(Customer.address.city) {
        City.name.value set cityName
        City.country.value set country.value
    }
}

fun setMultipleNestedValues() {
    val you = Customer.with().relocateToDifferentCity()
    you.cityName() shouldBe "Barcelona" andPrint
            "expected Barcelona but was ${you.cityName()}"

    you.country().value shouldBe "Spain" andPrint
            "expected Spain but was ${you.country().value}"

    val myBuddy = Customer.with()
    val buddyInAnotherCountry = myBuddy.relocateToCityAndCountry(
        City.with(name = Name("Istanbul")),
        Country(CountryEnum.TURKEY.name)
    )
    (buddyInAnotherCountry.country().value shouldBe CountryEnum.TURKEY.name) andPrint
            "expected TURKEY but was ${buddyInAnotherCountry.country().value}"

    buddyInAnotherCountry.cityName() shouldBe "Istanbul" andPrint
        "expected Istanbul but was ${buddyInAnotherCountry.cityName()}"

    val unlovedCousin = Customer.with()
    unlovedCousin.marryAndRelocate(
        cityName = "Gera",
        name = "Gustav Gans"
    )

    unlovedCousin.name() shouldBe "Gustav Gans" andPrint
        "expected Gustav Gans but was ${unlovedCousin.name()}"

    unlovedCousin.cityName() shouldBe "Gera" andPrint
        "expected Gera but was ${unlovedCousin.cityName()}"


    val neighbor = Customer.with().turn18AndMoveOut()
    neighbor.age() shouldBe 18 andPrint
        "expected 18 but was ${neighbor.age()}"

    neighbor.country().value shouldBe "Argentine" andPrint
        "expected Argentine but was ${neighbor.country().value}"

    neighbor.cityName() shouldBe "Rio de Janeiro" andPrint
        "expected Rio de Janeiro but was ${neighbor.cityName()}"
}

// helper infix methods
infix fun <T> T.andPrint(msg:String) = this.apply {
    println(msg)
}

