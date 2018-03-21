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

    export enum NotificationCategoryOption {
        CustomDismissAction = 'UNNotificationCategoryOptionCustomDismissAction',
        AllowInCarPlay = 'UNNotificationCategoryOptionAllowInCarPlay',
        PreviewsShowTitle = 'UNNotificationCategoryOptionHiddenPreviewsShowTitle',
        PreviewsShowSubtitle = 'UNNotificationCategoryOptionHiddenPreviewsShowSubtitle',
        None = 'UNNotificationCategoryOptionNone'
    }

    export enum NotificationActionOption {
        AuthenticationRequired = 'UNNotificationActionOptionAuthenticationRequired',
        Destructive = 'UNNotificationActionOptionDestructive',
        Foreground = 'UNNotificationActionOptionForeground',
        None = 'UNNotificationActionOptionNone'
    }

    export enum NotificationActionType {
        Default = 'UNNotificationActionTypeDefault',
        TextInput = 'UNNotificationActionTypeTextInput',
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
        fcm: {
            action?: string;
            tag?: string;
            icon?: string;
            color?: string;
            body: string;
            title?: string;
        };
        local_notification?: boolean;
        _notificationType: string;
        _actionIdentifier?: string;
        _userText?: string;
        finish(type?: string): void;
        [key: string]: any;
    }

    export interface LocalNotification {
        id?: string;
        title?: string;
        body: string;
        icon?: string;
        vibrate?: number;
        sound?: string;
        big_text?: string;
        sub_text?: string;
        color?: string;
        large_icon?: string;
        priority?: string;
        show_in_foreground?: boolean;
        click_action?: string;
        badge?: number;
        number?: number;
        ticker?: string;
        auto_cancel?: boolean;
        group?: string;
        picture?: string;
        ongoing?: boolean;
        lights?: boolean;
        [key: string]: any;
    }

    export interface ScheduleLocalNotification extends LocalNotification {
        id: string;
        fire_date: number;
        repeat_interval?: "week" | "day" | "hour"
    }

    export interface Subscription {
        remove(): void;
    }

    export interface NotificationAction {
        type: NotificationActionType;
        id: string;
        title?: string;
        textInputButtonTitle?: string;
        textInputPlaceholder?: string;
        options: NotificationActionOption | NotificationActionOption[];
    }

    export interface NotificationCategory {
        id: string;
        actions: NotificationAction[];
        intentIdentifiers: string[];
        hiddenPreviewsBodyPlaceholder?: string;
        options?: NotificationCategoryOption | NotificationCategoryOption[];
    }

    export class FCM {
        static requestPermissions(): Promise<void>;
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

        static setNotificationCategories(categories: NotificationCategory[]): void;
    }

    export default FCM;
}
