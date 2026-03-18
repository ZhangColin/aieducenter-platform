package com.aieducenter.account.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cartisan.core.exception.DomainException;
import com.aieducenter.account.domain.error.UserError;

/**
 * PhoneNumber 值对象测试。
 */
class PhoneNumberTest {

    // ========== 成功场景 ==========

    @Test
    void shouldCreatePhoneNumber_whenFormatValid() {
        // When
        PhoneNumber phone = new PhoneNumber("13812345678");

        // Then
        assertThat(phone.value()).isEqualTo("13812345678");
    }

    @Test
    void shouldCreatePhoneNumber_whenSecondDigitIs3() {
        // When
        PhoneNumber phone = new PhoneNumber("13123456789");

        // Then
        assertThat(phone.value()).isEqualTo("13123456789");
    }

    @Test
    void shouldCreatePhoneNumber_whenSecondDigitIs9() {
        // When
        PhoneNumber phone = new PhoneNumber("19123456789");

        // Then
        assertThat(phone.value()).isEqualTo("19123456789");
    }

    @Test
    void shouldCreatePhoneNumber_whenAllDigitsAfter() {
        // When
        PhoneNumber phone = new PhoneNumber("15900000000");

        // Then
        assertThat(phone.value()).isEqualTo("15900000000");
    }

    // ========== 失败场景 ==========

    @Test
    void shouldThrow_whenPhoneNumberIsNull() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber(null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldThrow_whenPhoneNumberTooShort() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber("1381234567"))  // 10 位
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldThrow_whenPhoneNumberTooLong() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber("138123456789"))  // 12 位
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldThrow_whenPhoneNumberNotStartWith1() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber("23812345678"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldThrow_whenPhoneNumberSecondDigitIs2() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber("12812345678"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldThrow_whenPhoneNumberSecondDigitIs0() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber("10812345678"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldThrow_whenPhoneNumberContainsNonDigit() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber("1381234567a"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldThrow_whenPhoneNumberContainsSpaces() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber("138 1234 5678"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldThrow_whenPhoneNumberContainsHyphens() {
        // When & Then
        assertThatThrownBy(() -> new PhoneNumber("138-1234-5678"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    // ========== 值对象相等性 ==========

    @Test
    void shouldEqual_whenSameValue() {
        // Given
        PhoneNumber phone1 = new PhoneNumber("13812345678");
        PhoneNumber phone2 = new PhoneNumber("13812345678");

        // Then
        assertThat(phone1).isEqualTo(phone2);
        assertThat(phone1.sameValueAs(phone2)).isTrue();
    }

    @Test
    void shouldNotEqual_whenDifferentValue() {
        // Given
        PhoneNumber phone1 = new PhoneNumber("13812345678");
        PhoneNumber phone2 = new PhoneNumber("13912345678");

        // Then
        assertThat(phone1).isNotEqualTo(phone2);
        assertThat(phone1.sameValueAs(phone2)).isFalse();
    }
}
