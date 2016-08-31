/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
package com.acessoatodos.places;

import java.util.List;

import com.acessoatodos.acessibility.AcessibilityVO;

import lombok.Getter;

/**
 * API response holding places information.
 */
@Getter
class PlaceVO {
    /**
     * The place id of place registered on google
     */
    String placeId;

    /**
     * The name of place
     */
    String name;

    /**
     * The position of latitude of place
     */
    Float latitude;

    /**
     * The position of longitude of place
     */
    Float longitude;

    /**
     * The list of acessibility codes registered
     */
    List<AcessibilityVO> acessibilities;

    /**
     * The list of descriptions of types of places registered on google
     */
    List<String> types;
}
