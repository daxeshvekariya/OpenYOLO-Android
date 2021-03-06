/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.protocol.internal;

import android.net.Uri;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Convenient hamcrest-based input validators.
 */
public final class CustomMatchers {

    /**
     * Matcher for web URIs (http or https).
     */
    public static Matcher<Uri> isWebUri() {
        return UriMatcher.WEB_MATCHER;
    }

    /**
     * Matcher for HTTPS web URIs.
     */
    public static Matcher<Uri> isHttpsUri() {
        return UriMatcher.HTTPS_ONLY_MATCHER;
    }

    /**
     * Matcher for HTTPS web URIs, in string form.
     */
    public static Matcher<String> isHttpsUriStr() {
        return StringUriMatcher.HTTPS_URI_STR;
    }

    /**
     * Matcher for OpenYOLO authentication methods, in string form.
     */
    public static Matcher<String> isValidAuthenticationMethod() {
        return StringUriMatcher.AUTHENTICATION_METHOD_STR;
    }

    /**
     * Matcher for OpenYOLO authentication methods, in parsed Uri form.
     */
    public static Matcher<Uri> isValidAuthenticationMethodUri() {
        return UriMatcher.AUTHENTICATION_METHOD;
    }

    /**
     * Matcher for OpenYOLO authentication domains, in string form.
     */
    public static Matcher<String> isValidAuthenticationDomain() {
        return StringUriMatcher.AUTHENTICATION_DOMAIN_STR;
    }

    /**
     * Matcher for OpenYOLO authentication domains, in parsed Uri form.
     */
    public static Matcher<Uri> isValidAuthenticationDomainUri() {
        return UriMatcher.AUTHENTICATION_DOMAIN;
    }

    /**
     * Matcher for null values, or those which match the provided matcher.
     */
    public static <T> Matcher<T> nullOr(Matcher<T> matcher) {
        return new NullOrOtherMatcher<>(matcher);
    }

    /**
     * Matcher which only accepts non-null, non-empty strings.
     */
    public static Matcher<String> notNullOrEmptyString() {
        return NotNullOrEmptyStringMatcher.INSTANCE;
    }

    private static final class NotNullOrEmptyStringMatcher extends BaseMatcher<String> {

        private static final NotNullOrEmptyStringMatcher INSTANCE =
                new NotNullOrEmptyStringMatcher();

        @Override
        public boolean matches(Object item) {
            if (item == null) {
                return false;
            }

            if (!(item instanceof String)) {
                return false;
            }

            return !TextUtils.isEmpty((String)item);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is not a null or empty string");
        }
    }

    private static final class NullOrOtherMatcher<T> extends BaseMatcher<T> {

        final Matcher<T> mMatcher;

        NullOrOtherMatcher(Matcher<T> matcher) {
            mMatcher = matcher;
        }

        @Override
        public boolean matches(Object item) {
            return item == null || mMatcher.matches(item);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("null or ").appendDescriptionOf(mMatcher);
        }
    }

    private static final class StringUriMatcher extends CustomTypeSafeMatcher<String> {

        static final StringUriMatcher HTTPS_URI_STR =
                new StringUriMatcher(UriMatcher.HTTPS_ONLY_MATCHER);

        static final StringUriMatcher AUTHENTICATION_DOMAIN_STR =
                new StringUriMatcher(UriMatcher.AUTHENTICATION_DOMAIN);

        static final StringUriMatcher AUTHENTICATION_METHOD_STR =
                new StringUriMatcher(UriMatcher.AUTHENTICATION_METHOD);

        private final UriMatcher mUriMatcher;

        StringUriMatcher(UriMatcher matcher) {
            super(matcher.mUriDescriptor);
            mUriMatcher = matcher;
        }

        @Override
        protected boolean matchesSafely(String item) {
            Uri uri = Uri.parse(item);
            return mUriMatcher.matchesSafely(uri);
        }
    }

    private static final class UriMatcher extends CustomTypeSafeMatcher<Uri> {

        static final String SCHEME_HTTP = "http";
        static final String SCHEME_HTTPS = "https";

        static final UriMatcher AUTHENTICATION_DOMAIN = new UriMatcher(
                Collections.<String>emptySet(),
                false,
                "an authentication domain");

        static final UriMatcher AUTHENTICATION_METHOD = new UriMatcher(
                Collections.<String>emptySet(),
                false,
                "an authentication method");

        static final UriMatcher HTTPS_ONLY_MATCHER = new UriMatcher(
                Collections.singleton(SCHEME_HTTPS),
                true,
                "an https URL");

        static final UriMatcher WEB_MATCHER = new UriMatcher(
                new HashSet<>(Arrays.asList(SCHEME_HTTP, SCHEME_HTTPS)),
                true,
                "a web URL");

        final Set<String> mPermittedSchemes;
        final boolean mAllowPathQueryOrFragment;
        final String mUriDescriptor;

        UriMatcher(
                Set<String> permittedSchemes,
                boolean allowPathQueryOrFragment,
                String uriDescriptor) {
            super(uriDescriptor);
            mPermittedSchemes = permittedSchemes;
            mAllowPathQueryOrFragment = allowPathQueryOrFragment;
            mUriDescriptor = uriDescriptor;
        }

        @Override
        protected boolean matchesSafely(Uri item) {
            if (!item.isAbsolute()
                    || !item.isHierarchical()
                    || TextUtils.isEmpty(item.getScheme())
                    || TextUtils.isEmpty(item.getAuthority())) {
                return false;
            }

            if (!mPermittedSchemes.isEmpty() && !mPermittedSchemes.contains(item.getScheme())) {
                return false;
            }

            if (mAllowPathQueryOrFragment) {
                return true;
            }

            return TextUtils.isEmpty(item.getPath())
                    && item.getQuery() == null
                    && item.getFragment() == null;
        }
    }
}
