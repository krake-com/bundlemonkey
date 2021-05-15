/*
 * Tentackle - https://tentackle.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.tentackle.bundlemonkey.common;

import org.tentackle.common.BundleFactory;
import org.tentackle.common.DefaultBundleFactory;
import org.tentackle.common.Service;

/**
 * Disable the Bundle factory of the i18n module.<br>
 * See also <code>&#64;ModuleOrdinal</code> annotation in <code>module-info</code>.
 */
@Service(BundleFactory.class)
public class BundleMonkeyBundleFactory extends DefaultBundleFactory {

}
