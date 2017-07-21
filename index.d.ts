declare module "react-native-fcm" {

    type FCMEventType = "FCMTokenRefreshed" | "FCMNotificationReceived" | 'FCMDirectChannelConnectionChanged';
    export module FCMEvent {
        const RefreshToken = "FCMTokenRefreshed";
        const Notification = "FCMNotificationReceived";
        const DirectChannelConnectionChanged: 'FCMDirectChannelConnectionChanged'
    }

    export module RemoteNotificationResult {
        const NewData = "UIBackgroundFetchResultNewData";
        const NoData = "UIBackgroundFetchResultNoData";
        const ResultFailed = "UIBackgroundFetchResultFailed";
    }

    export module WillPresentNotificationResult {
        const All = "UNNotificationPresentationOptionAll";
        const None = "UNNotificationPresentationOptionNone";
    }

    export module NotificationType {
        const Remote = "remote_notification";
        const NotificationResponse = "notification_response";
        const WillPresent = "will_present_notification";
        const Local = "local_notification";
    }

  export interface Notification {
        collapse_key: string;
        opened_from_tray: boolean;
        from: string;
        notification: {
            title?: string
            body: string;
            icon: string;
        };
        _notificationType: string;
        finish(type?: string): void;
    }

    export interface LocalNotification {
        title?: string;
        body: string;
        icon?: string;
        vibrate?: number;
        sound?: string;
        big_text?: string;
        large_icon?: string;
        priority?: string
    }

    export interface ScheduleLocalNotification extends LocalNotification{
        id: string;
        fire_date: number
    }

    export interface Subscription {
        remove(): void;
    }

    export class FCM {
        static requestPermissions(): void;
        static getFCMToken(): Promise<string>;
        static on(event: "FCMTokenRefreshed", handler: (token: string) => void): Subscription;
        static on(event: "FCMNotificationReceived", handler: (notification: Notification) => void): Subscription;
        static subscribeToTopic(topic: string): void;
        static unsubscribeFromTopic(topic: string): void;
        static getInitialNotification(): Promise<Notification>;
        static presentLocalNotification(notification: LocalNotification): void;

        static scheduleLocalNotification(schedule: ScheduleLocalNotification): void;
        static getScheduledLocalNotifications(): Promise<LocalNotification>;

        static removeAllDeliveredNotifications(): void;
        static removeDeliveredNotification(id: string): void;

        static cancelAllLocalNotifications(): void;
        static cancelLocalNotification(id: string): string;

        static setBadgeNumber(badge: number): void;
        static getBadgeNumber(): Promise<number>;
        static send(id: string, data: any): void;

        static enableDirectChannel(): void
        static isDirectChannelEstablished(): Promise<boolean>
        static getAPNSToken(): Promise<string>
    }

    export default FCM;
}
