//
//  Created by Daksh on 27/08/20.
//  Copyright © 2020 The One Technologies. All rights reserved.
//

import Foundation

class SOMessage: Codable {
    
    enum SOSwitchType: String, Codable {
        case start, stop
    }
    
    var id : String?
    var msg: SOSwitchType?

}


