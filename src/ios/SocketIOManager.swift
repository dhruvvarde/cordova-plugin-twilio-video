//
//  SocketIOManager.swift
//
//  Created by Daksh on 31/08/20.
//  Copyright © 2020 The One Technologies. All rights reserved.
//

import Foundation
import SocketIO

struct SocketSettings {
    static let socketURL = "https://devapsparseserver.iron.fit"
}

@objcMembers public class SocketIOManager : NSObject {
    
    public static let shared: SocketIOManager = SocketIOManager()
    
    typealias SocketDidChangeConnectionState = ((SocketClientEvent)->())
    var parseId: String = ""
    
    enum AppEvent {
         
        case liveCaption
        case closedCaptionStatus(userId: String)
        case messageClosedCaption(userId: String)

        var name : String {
            switch self {
            case .liveCaption:
                return "live_caption"
            case .closedCaptionStatus(let userId):
                return "cc_\(userId)"
            case .messageClosedCaption(let userId):
                return "msg_\(userId)"
            }
        }
    }
    
    public lazy var viewModel = {
        SocketIOViewModel(_manager: self)
    }()
    
    let manager : SocketManager = SocketManager(
        socketURL: URL(string: SocketSettings.socketURL)!,
        config: config)
    var observedEvents: [AppEvent] = []
    
    static var config : SocketIOClientConfiguration = {
        SocketIOClientConfiguration(
            arrayLiteral:
            .forceWebsockets(true)
//            .log(true)
        )
    }()
    
    lazy var socket : SocketIOClient = {
        self.manager.defaultSocket
    }()
    
    var didChangeConnectionState : SocketDidChangeConnectionState?
    var didReceiveMessage : ((Any)->())?
    
    deinit {
        debugPrint("\(self):\(#function)")
    }
    
    //MARK: - Configure
    public func configure(userId: String) {
        debugPrint("\(self):\(#function)")
        parseId = userId
        
        configureConnection()
        configureEvents()
    }
    
    func clear(_ completion: (()->())? = nil) {
        debugPrint("\(self):\(#function)")
        
        didChangeConnectionState = nil
        closeEvents(events: observedEvents)
        closeConnection { (e) in
            if e == .disconnect {
                completion?()
            }
        }
    }
}

extension SocketIOManager {
    //MARK: - Configure Connection
    
    func configureConnection() {
        debugPrint("\(self):\(#function)")
       
        socket.onAny { [weak self] (e) in
            self?.handleConnectionState(e)
        }
        
        startConnection()
    }
    
    func startConnection() {
        debugPrint("\(self):\(#function)")
        
        switch socket.status {
        case .notConnected, .disconnected:
            establishConnection()
            break
            
        default:
            break
        }
    }
    
    //MARK: - Handle Connection State
    
    func handleConnectionState(_ e: SocketAnyEvent) {
        debugPrint("\(self):\(#function), SocketAnyEvent: \(e)")
        
        guard let event: SocketClientEvent = SocketClientEvent(rawValue: e.event) else { return }
        didChangeConnectionState?(event)
        
        guard let item = e.items?.first else { return }
        debugPrint("\(self):\(#function) event: \(e.event), item:\(item)")
        
        switch event {
        case .connect:
            debugPrint("\(self):\(#function) connected successfully.")
            
        default:
            break
        }
    }
    
}

extension SocketIOManager {
    //MARK: - Configure Events
    
    fileprivate func configureEvents() {
        debugPrint("\(self):\(#function)")
        
        observedEvents = [.closedCaptionStatus(userId: parseId), .messageClosedCaption(userId: parseId)]
        observedEvents.forEach { event in
            configureEvent(event: event, completion: handleEvent(event:data:ack:))
        }
    }
    
    fileprivate func configureEvent(event: AppEvent, completion: ((AppEvent, [Any], SocketAckEmitter)->Void)? = nil) {
        debugPrint("\(self):\(#function): event: \(event.name)")
        
        socket.on(event.name) { data, ack in
            completion?(event, data, ack)
        }
    }
    
    //MARK: - Handle Events
    
    fileprivate func handleEvent(event: AppEvent, data: [Any], ack: SocketAckEmitter) {
        debugPrint("\(self):\(#function) event: \(event.name), data: \(data)")
        
        guard let result = data.first as? String else { return }
        switch event {
        case .closedCaptionStatus:
            NotificationCenter.default.post(name: NSNotification.Name("SocketIOManager_ClosedCaptionStatus"), object: result)
            break
        case .messageClosedCaption:
            NotificationCenter.default.post(name: NSNotification.Name("SocketIOManager_MessageClosedCaption"), object: result)
            break
        default: break
        }
    }
    
}
